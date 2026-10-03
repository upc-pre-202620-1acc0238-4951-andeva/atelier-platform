package com.andeva.atelier.platform.shared.domain.model.valueobjects;

import com.andeva.atelier.platform.shared.domain.exceptions.BusinessRuleValidationException;
import com.andeva.atelier.platform.shared.domain.exceptions.CurrencyMismatchException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Comprehensive unit test suite covering all Value Objects of the Shared Bounded Context.
 * Verifies invariants, boundary values, Banker's Rounding, Haversine formula,
 * SUNAT Modulo 11 checksum, RFC 5322, and E.164 compliance.
 *
 * @author Joel Huamani Estefanero
 */
@DisplayName("Shared Domain Value Objects Tests")
class ValueObjectsTest {

    @Nested
    @DisplayName("Money Tests")
    class MoneyTests {

        @Test
        @DisplayName("Should create Money with two decimals and Half-Even (banker's) rounding")
        void shouldCreateMoneyWithTwoDecimalsAndHalfEvenRounding() {
            // Half-Even: .555 -> .56 (rounds up because preceding digit 5 is odd)
            Money m1 = Money.of(new BigDecimal("10.555"), Currency.PEN);
            assertThat(m1.amount()).isEqualByComparingTo("10.56");
            assertThat(m1.currency()).isEqualTo(Currency.PEN);
            assertThat(m1.toString()).isEqualTo("S/. 10.56");

            // Half-Even: .545 -> .54 (rounds down because preceding digit 4 is even)
            Money m2 = Money.of(new BigDecimal("10.545"), Currency.PEN);
            assertThat(m2.amount()).isEqualByComparingTo("10.54");

            // Half-Even: .565 -> .56 (rounds down because preceding digit 6 is even)
            Money m3 = Money.of(new BigDecimal("10.565"), Currency.USD);
            assertThat(m3.amount()).isEqualByComparingTo("10.56");
            assertThat(m3.toString()).isEqualTo("$ 10.56");

            // Half-Even: .575 -> .58 (rounds up because preceding digit 7 is odd)
            Money m4 = Money.of(new BigDecimal("10.575"), Currency.USD);
            assertThat(m4.amount()).isEqualByComparingTo("10.58");

            // Integer scaled to two decimals
            Money m5 = Money.of(10, Currency.PEN);
            assertThat(m5.amount()).isEqualByComparingTo("10.00");
        }

        @Test
        @DisplayName("Should throw NullPointerException when amount or currency is null")
        void shouldThrowWhenAmountOrCurrencyIsNull() {
            assertThatThrownBy(() -> Money.of((BigDecimal) null, Currency.PEN))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Monetary amount cannot be null");

            assertThatThrownBy(() -> Money.of(new BigDecimal("10.00"), null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Currency cannot be null");
        }

        @Test
        @DisplayName("Should add and subtract amounts with same currency")
        void shouldAddAndSubtractSameCurrency() {
            Money m1 = Money.soles(100.50);
            Money m2 = Money.soles(50.25);

            Money added = m1.add(m2);
            assertThat(added.amount()).isEqualByComparingTo("150.75");
            assertThat(added.currency()).isEqualTo(Currency.PEN);

            Money subtracted = m1.subtract(m2);
            assertThat(subtracted.amount()).isEqualByComparingTo("50.25");
            assertThat(subtracted.currency()).isEqualTo(Currency.PEN);

            Money exactZero = m1.subtract(m1);
            assertThat(exactZero.amount()).isEqualByComparingTo("0.00");
            assertThat(exactZero.isZero()).isTrue();

            Money negative = m2.subtract(m1);
            assertThat(negative.amount()).isEqualByComparingTo("-50.25");
            assertThat(negative.isPositive()).isFalse();
        }

        @Test
        @DisplayName("Should throw CurrencyMismatchException when operating different currencies")
        void shouldThrowExceptionWhenOperatingDifferentCurrencies() {
            Money soles = Money.soles(100.00);
            Money dollars = Money.dollars(100.00);

            assertThatThrownBy(() -> soles.add(dollars))
                    .isInstanceOf(CurrencyMismatchException.class)
                    .satisfies(ex -> assertThat(((CurrencyMismatchException) ex).errorCode()).isEqualTo("CURRENCY_MISMATCH"));

            assertThatThrownBy(() -> soles.subtract(dollars))
                    .isInstanceOf(CurrencyMismatchException.class);

            assertThatThrownBy(() -> soles.isGreaterThan(dollars))
                    .isInstanceOf(CurrencyMismatchException.class);

            assertThatThrownBy(() -> soles.isLessThan(dollars))
                    .isInstanceOf(CurrencyMismatchException.class);
        }

        @Test
        @DisplayName("Should throw NullPointerException when operating with null Money operand")
        void shouldThrowWhenOperatingWithNullMoney() {
            Money soles = Money.soles(100.00);

            assertThatThrownBy(() -> soles.add(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Monetary amount to compare cannot be null");

            assertThatThrownBy(() -> soles.subtract(null))
                    .isInstanceOf(NullPointerException.class);

            assertThatThrownBy(() -> soles.isGreaterThan(null))
                    .isInstanceOf(NullPointerException.class);

            assertThatThrownBy(() -> soles.isLessThan(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should multiply and divide with Half-Even rounding")
        void shouldDivideAndMultiplyCorrectly() {
            Money m = Money.soles(100.00);

            // 100 / 3 = 33.3333... -> 33.33
            Money divided = m.divide(BigDecimal.valueOf(3));
            assertThat(divided.amount()).isEqualByComparingTo("33.33");

            // 10.55 / 2 = 5.275 -> 5.28 (preceding odd digit 7 rounds up to 8)
            Money mOdd = Money.soles(10.55);
            assertThat(mOdd.divide(BigDecimal.valueOf(2)).amount()).isEqualByComparingTo("5.28");

            // 10.53 / 2 = 5.265 -> 5.26 (preceding even digit 6 rounds down to 6)
            Money mEven = Money.soles(10.53);
            assertThat(mEven.divide(BigDecimal.valueOf(2)).amount()).isEqualByComparingTo("5.26");

            // Multiplication by double
            Money multipliedDouble = m.multiply(1.5);
            assertThat(multipliedDouble.amount()).isEqualByComparingTo("150.00");

            // Multiplication by BigDecimal
            Money multipliedBigDecimal = m.multiply(new BigDecimal("2.5"));
            assertThat(multipliedBigDecimal.amount()).isEqualByComparingTo("250.00");

            // Null checks
            assertThatThrownBy(() -> m.multiply((BigDecimal) null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Multiplication factor cannot be null");

            assertThatThrownBy(() -> m.divide(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Divisor cannot be null");
        }

        @Test
        @DisplayName("Should throw ArithmeticException when dividing by zero")
        void shouldThrowWhenDividingByZero() {
            Money m = Money.soles(100.00);

            assertThatThrownBy(() -> m.divide(BigDecimal.ZERO))
                    .isInstanceOf(ArithmeticException.class)
                    .hasMessage("Cannot divide a monetary magnitude by zero");

            assertThatThrownBy(() -> m.divide(new BigDecimal("0.00")))
                    .isInstanceOf(ArithmeticException.class)
                    .hasMessage("Cannot divide a monetary magnitude by zero");
        }

        @Test
        @DisplayName("Should perform comparisons correctly")
        void shouldCompareMoneyAmountsCorrectly() {
            Money higher = Money.soles(100.00);
            Money lower = Money.soles(50.00);
            Money equal = Money.soles(100.00);

            assertThat(higher.isGreaterThan(lower)).isTrue();
            assertThat(lower.isGreaterThan(higher)).isFalse();
            assertThat(higher.isGreaterThan(equal)).isFalse();

            assertThat(lower.isLessThan(higher)).isTrue();
            assertThat(higher.isLessThan(lower)).isFalse();
            assertThat(higher.isLessThan(equal)).isFalse();

            assertThat(higher.isPositive()).isTrue();
            assertThat(Money.soles(0.00).isPositive()).isFalse();
            assertThat(Money.soles(-5.00).isPositive()).isFalse();

            assertThat(Money.soles(0.00).isZero()).isTrue();
            assertThat(higher.isZero()).isFalse();
        }

        @Test
        @DisplayName("Should verify static constants and factory methods")
        void shouldVerifyStaticConstantsAndFactoryMethods() {
            assertThat(Money.ZERO_PEN.amount()).isEqualByComparingTo("0.00");
            assertThat(Money.ZERO_PEN.currency()).isEqualTo(Currency.PEN);
            assertThat(Money.ZERO_PEN.isZero()).isTrue();

            assertThat(Money.ZERO_USD.amount()).isEqualByComparingTo("0.00");
            assertThat(Money.ZERO_USD.currency()).isEqualTo(Currency.USD);
            assertThat(Money.ZERO_USD.isZero()).isTrue();

            Money fromDouble = Money.dollars(45.50);
            assertThat(fromDouble.amount()).isEqualByComparingTo("45.50");
            assertThat(fromDouble.currency()).isEqualTo(Currency.USD);

            Money fromBigDecimal = Money.dollars(new BigDecimal("75.25"));
            assertThat(fromBigDecimal.amount()).isEqualByComparingTo("75.25");
        }
    }

    @Nested
    @DisplayName("Quantity Tests")
    class QuantityTests {

        @Test
        @DisplayName("Should create Quantity with scale 2 and Half-Even rounding")
        void shouldCreateQuantityCorrectly() {
            Quantity q1 = Quantity.of(5.50, MeasurementUnit.LITER);
            assertThat(q1.value()).isEqualByComparingTo("5.50");
            assertThat(q1.unit()).isEqualTo(MeasurementUnit.LITER);

            Quantity qUnits = Quantity.ofUnits(10);
            assertThat(qUnits.value()).isEqualByComparingTo("10.00");
            assertThat(qUnits.unit()).isEqualTo(MeasurementUnit.UNIT);

            Quantity qRoundUp = Quantity.of(new BigDecimal("5.555"), MeasurementUnit.KILOGRAM);
            assertThat(qRoundUp.value()).isEqualByComparingTo("5.56");

            Quantity qRoundDown = Quantity.of(new BigDecimal("5.545"), MeasurementUnit.KILOGRAM);
            assertThat(qRoundDown.value()).isEqualByComparingTo("5.54");
        }

        @Test
        @DisplayName("Should throw NullPointerException when value or unit is null")
        void shouldThrowWhenValueOrUnitIsNull() {
            assertThatThrownBy(() -> Quantity.of((BigDecimal) null, MeasurementUnit.UNIT))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Quantitative value cannot be null");

            assertThatThrownBy(() -> Quantity.of(BigDecimal.TEN, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Measurement unit cannot be null");
        }

        @Test
        @DisplayName("Should reject negative quantity amounts")
        void shouldThrowWhenNegative() {
            assertThatThrownBy(() -> Quantity.of(-1.0, MeasurementUnit.UNIT))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Quantity cannot be negative: -1.0");

            assertThatThrownBy(() -> Quantity.of(new BigDecimal("-0.01"), MeasurementUnit.LITER))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> Quantity.ofUnits(-5))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should allow zero quantity")
        void shouldAllowZeroQuantity() {
            Quantity zero = Quantity.of(0.0, MeasurementUnit.UNIT);
            assertThat(zero.value()).isEqualByComparingTo("0.00");
        }

        @Test
        @DisplayName("Should add quantities with same unit")
        void shouldAddWhenSameUnit() {
            Quantity q1 = Quantity.of(5.50, MeasurementUnit.LITER);
            Quantity q2 = Quantity.of(2.25, MeasurementUnit.LITER);
            Quantity result = q1.add(q2);
            assertThat(result.value()).isEqualByComparingTo("7.75");
            assertThat(result.unit()).isEqualTo(MeasurementUnit.LITER);
        }

        @Test
        @DisplayName("Should subtract when stock is sufficient or exact")
        void shouldSubtractWhenSufficientOrExact() {
            Quantity q1 = Quantity.of(10.0, MeasurementUnit.KILOGRAM);
            Quantity q2 = Quantity.of(4.0, MeasurementUnit.KILOGRAM);
            Quantity result = q1.subtract(q2);
            assertThat(result.value()).isEqualByComparingTo("6.00");

            // Exact stock deduction
            Quantity exactResult = q1.subtract(q1);
            assertThat(exactResult.value()).isEqualByComparingTo("0.00");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when stock is insufficient")
        void shouldThrowWhenInsufficientStock() {
            Quantity q1 = Quantity.of(3.0, MeasurementUnit.KILOGRAM);
            Quantity q2 = Quantity.of(5.0, MeasurementUnit.KILOGRAM);
            assertThatThrownBy(() -> q1.subtract(q2))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Insufficient stock to deduct: current 3.00, required 5.00");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException on unit mismatch for add, subtract, and hasSufficient")
        void shouldThrowWhenDifferentUnits() {
            Quantity q1 = Quantity.of(10.0, MeasurementUnit.LITER);
            Quantity q2 = Quantity.of(10.0, MeasurementUnit.GALLON);

            assertThatThrownBy(() -> q1.add(q2))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Measurement unit mismatch: LITER vs GALLON");

            assertThatThrownBy(() -> q1.subtract(q2))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Measurement unit mismatch: LITER vs GALLON");

            assertThatThrownBy(() -> q1.hasSufficient(q2))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Measurement unit mismatch: LITER vs GALLON");
        }

        @Test
        @DisplayName("Should correctly evaluate stock sufficiency")
        void shouldCheckSufficiencyCorrectly() {
            Quantity available = Quantity.of(10.0, MeasurementUnit.UNIT);
            Quantity requiredLesser = Quantity.of(5.0, MeasurementUnit.UNIT);
            Quantity requiredExact = Quantity.of(10.0, MeasurementUnit.UNIT);
            Quantity requiredGreater = Quantity.of(15.0, MeasurementUnit.UNIT);

            assertThat(available.hasSufficient(requiredLesser)).isTrue();
            assertThat(available.hasSufficient(requiredExact)).isTrue();
            assertThat(available.hasSufficient(requiredGreater)).isFalse();
        }

        @Test
        @DisplayName("Should throw NullPointerException when operating with null Quantity operand")
        void shouldThrowWhenOperatingWithNullQuantity() {
            Quantity q = Quantity.of(5.0, MeasurementUnit.METER);

            assertThatThrownBy(() -> q.add(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Comparison quantity cannot be null");

            assertThatThrownBy(() -> q.subtract(null))
                    .isInstanceOf(NullPointerException.class);

            assertThatThrownBy(() -> q.hasSufficient(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Mileage Tests")
    class MileageTests {

        @Test
        @DisplayName("Should create valid mileage for zero and positive values")
        void shouldCreateValidMileage() {
            Mileage mPositive = Mileage.of(45000);
            assertThat(mPositive.value()).isEqualTo(45000);
            assertThat(mPositive.toString()).isEqualTo("45000 km");

            Mileage mZero = Mileage.of(0);
            assertThat(mZero.value()).isEqualTo(0);
            assertThat(mZero.toString()).isEqualTo("0 km");
        }

        @Test
        @DisplayName("Should reject negative mileage")
        void shouldThrowWhenMileageIsNegative() {
            assertThatThrownBy(() -> Mileage.of(-1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Automotive mileage cannot be negative: -1");

            assertThatThrownBy(() -> Mileage.of(-100))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Automotive mileage cannot be negative: -100");
        }

        @Test
        @DisplayName("Should calculate absolute difference symmetrically")
        void shouldCalculateDifference() {
            Mileage m1 = Mileage.of(50000);
            Mileage m2 = Mileage.of(45000);

            // Absolute difference regardless of order
            assertThat(m1.difference(m2)).isEqualTo(5000);
            assertThat(m2.difference(m1)).isEqualTo(5000);

            // Difference with self is 0
            assertThat(m1.difference(m1)).isEqualTo(0);
        }

        @Test
        @DisplayName("Should compare mileages correctly")
        void shouldCompareMileagesCorrectly() {
            Mileage m1 = Mileage.of(50000);
            Mileage m2 = Mileage.of(45000);
            Mileage m3 = Mileage.of(50000);

            assertThat(m1.isGreaterThan(m2)).isTrue();
            assertThat(m2.isGreaterThan(m1)).isFalse();
            assertThat(m1.isGreaterThan(m3)).isFalse();
        }

        @Test
        @DisplayName("Should support equality and hash code contracts")
        void shouldSupportRecordEqualityAndHashCode() {
            Mileage m1 = Mileage.of(30000);
            Mileage m2 = Mileage.of(30000);
            Mileage m3 = Mileage.of(40000);

            assertThat(m1).isEqualTo(m2);
            assertThat(m1.hashCode()).isEqualTo(m2.hashCode());
            assertThat(m1).isNotEqualTo(m3);
        }
    }

    @Nested
    @DisplayName("Strongly Typed IDs Tests")
    class StronglyTypedIdsTests {

        @Test
        @DisplayName("Should generate unique IDs for all bounded context entities")
        void shouldGenerateAndInstantiateIds() {
            UUID uuid = UUID.randomUUID();
            TenantId tenantId = TenantId.of(uuid);
            BranchId branchId = BranchId.generate();
            CustomerId customerId = CustomerId.generate();
            VehicleId vehicleId = VehicleId.generate();
            UserId userId = UserId.generate();

            assertThat(tenantId.value()).isEqualTo(uuid);
            assertThat(branchId.value()).isNotNull();
            assertThat(customerId.value()).isNotNull();
            assertThat(vehicleId.value()).isNotNull();
            assertThat(userId.value()).isNotNull();

            // Verify unique generation
            assertThat(TenantId.generate()).isNotEqualTo(TenantId.generate());
            assertThat(BranchId.generate()).isNotEqualTo(BranchId.generate());
            assertThat(CustomerId.generate()).isNotEqualTo(CustomerId.generate());
            assertThat(VehicleId.generate()).isNotEqualTo(VehicleId.generate());
            assertThat(UserId.generate()).isNotEqualTo(UserId.generate());
        }

        @Test
        @DisplayName("Should parse IDs from valid UUID strings")
        void shouldInstantiateFromString() {
            UUID uuid = UUID.randomUUID();
            String uuidString = uuid.toString();

            assertThat(TenantId.of(uuidString).value()).isEqualTo(uuid);
            assertThat(BranchId.of(uuidString).value()).isEqualTo(uuid);
            assertThat(CustomerId.of(uuidString).value()).isEqualTo(uuid);
            assertThat(VehicleId.of(uuidString).value()).isEqualTo(uuid);
            assertThat(UserId.of(uuidString).value()).isEqualTo(uuid);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when parsing malformed UUID string")
        void shouldThrowWhenParsingInvalidUuidString() {
            assertThatThrownBy(() -> TenantId.of("not-a-valid-uuid"))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> BranchId.of("12345"))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> CustomerId.of(""))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> VehicleId.of("abc"))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> UserId.of("invalid"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw NullPointerException when UUID or string representation is null")
        void shouldThrowWhenIdIsNull() {
            assertThatThrownBy(() -> TenantId.of((UUID) null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Workshop (Tenant) identifier cannot be null");
            assertThatThrownBy(() -> TenantId.of((String) null))
                    .isInstanceOf(NullPointerException.class);

            assertThatThrownBy(() -> BranchId.of((UUID) null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Branch identifier cannot be null");
            assertThatThrownBy(() -> BranchId.of((String) null))
                    .isInstanceOf(NullPointerException.class);

            assertThatThrownBy(() -> CustomerId.of((UUID) null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Customer identifier cannot be null");
            assertThatThrownBy(() -> CustomerId.of((String) null))
                    .isInstanceOf(NullPointerException.class);

            assertThatThrownBy(() -> VehicleId.of((UUID) null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Vehicle identifier cannot be null");
            assertThatThrownBy(() -> VehicleId.of((String) null))
                    .isInstanceOf(NullPointerException.class);

            assertThatThrownBy(() -> UserId.of((UUID) null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("User account identifier cannot be null");
            assertThatThrownBy(() -> UserId.of((String) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should support equality and hash code across identical IDs")
        void shouldSupportEqualityAndHashCode() {
            UUID uuid = UUID.randomUUID();
            TenantId t1 = TenantId.of(uuid);
            TenantId t2 = TenantId.of(uuid.toString());

            assertThat(t1).isEqualTo(t2);
            assertThat(t1.hashCode()).isEqualTo(t2.hashCode());
        }
    }

    @Nested
    @DisplayName("GeoPoint & DistanceMeters Tests")
    class GeoPointTests {

        @Test
        @DisplayName("Should create GeoPoint at exact WGS84 boundary coordinates")
        void shouldCreateGeoPointAtWGS84Boundaries() {
            // Latitude bounds: [-90.0, 90.0]
            assertThat(GeoPoint.of(-90.0, 0.0).latitude()).isEqualTo(-90.0);
            assertThat(GeoPoint.of(90.0, 0.0).latitude()).isEqualTo(90.0);

            // Longitude bounds: [-180.0, 180.0]
            assertThat(GeoPoint.of(0.0, -180.0).longitude()).isEqualTo(-180.0);
            assertThat(GeoPoint.of(0.0, 180.0).longitude()).isEqualTo(180.0);

            // Origin
            GeoPoint origin = GeoPoint.of(0.0, 0.0);
            assertThat(origin.latitude()).isEqualTo(0.0);
            assertThat(origin.longitude()).isEqualTo(0.0);
        }

        @ParameterizedTest(name = "Lat {0}, Lon {1} should be out of bounds")
        @CsvSource({
                "90.0001, 0.0",
                "-90.0001, 0.0",
                "95.0, 0.0",
                "-95.0, 0.0",
                "0.0, 180.0001",
                "0.0, -180.0001",
                "0.0, 185.0",
                "0.0, -185.0"
        })
        @DisplayName("Should throw IllegalArgumentException when coordinates exceed WGS84 bounds")
        void shouldThrowWhenCoordinatesOutOfWGS84Bounds(double lat, double lon) {
            assertThatThrownBy(() -> GeoPoint.of(lat, lon))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should calculate Haversine distance accurately")
        void shouldCalculateHaversineDistanceAccurately() {
            // Lima centro: -12.046374, -77.042793
            // Miraflores: -12.121703, -77.029813
            // Approximate distance ~8.5 km (8500 m)
            GeoPoint limaCentro = GeoPoint.of(-12.046374, -77.042793);
            GeoPoint miraflores = GeoPoint.of(-12.121703, -77.029813);

            DistanceMeters distance = limaCentro.distanceTo(miraflores);
            assertThat(distance.value()).isBetween(8000.0, 9000.0);

            // Symmetry: distance from A to B equals distance from B to A
            DistanceMeters reverseDistance = miraflores.distanceTo(limaCentro);
            assertThat(reverseDistance.value()).isCloseTo(distance.value(), org.assertj.core.data.Offset.offset(0.001));

            // Distance to self is 0 meters
            DistanceMeters selfDistance = limaCentro.distanceTo(limaCentro);
            assertThat(selfDistance.value()).isEqualTo(0.0);

            // Null target throws NullPointerException
            assertThatThrownBy(() -> limaCentro.distanceTo(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Destination geographic point cannot be null");
        }

        @Test
        @DisplayName("Should evaluate DistanceMeters and threshold checks")
        void shouldEvaluateDistanceMetersAndThresholds() {
            DistanceMeters dZero = DistanceMeters.of(0.0);
            assertThat(dZero.value()).isEqualTo(0.0);
            assertThat(dZero.isWithinThreshold(0.0)).isTrue();

            DistanceMeters d = DistanceMeters.of(500.0);
            assertThat(d.isWithinThreshold(600.0)).isTrue();
            assertThat(d.isWithinThreshold(500.0)).isTrue(); // Exact boundary check
            assertThat(d.isWithinThreshold(499.99)).isFalse();

            // Reject negative distance
            assertThatThrownBy(() -> DistanceMeters.of(-0.01))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Distance in meters cannot be negative: -0.01");
        }
    }

    @Nested
    @DisplayName("TaxId Tests")
    class TaxIdTests {

        @Test
        @DisplayName("Should validate 8-digit DNI and apply whitespace trimming")
        void shouldValidateDniCorrectly() {
            TaxId dni = TaxId.dni("12345678");
            assertThat(dni.value()).isEqualTo("12345678");
            assertThat(dni.type()).isEqualTo(TaxIdType.DNI);

            // Trimming verification
            TaxId trimmedDni = TaxId.dni("  87654321  ");
            assertThat(trimmedDni.value()).isEqualTo("87654321");
            assertThat(trimmedDni.type()).isEqualTo(TaxIdType.DNI);

            TaxId zeroDni = TaxId.dni("00000000");
            assertThat(zeroDni.value()).isEqualTo("00000000");
        }

        @ParameterizedTest(name = "Invalid DNI: {0}")
        @ValueSource(strings = {
                "1234567",       // 7 digits (too short)
                "123456789",     // 9 digits (too long)
                "1234567A",      // contains alphanumeric character
                "12-45678",      // contains punctuation
                "        ",      // empty/blank after trim
                ""               // empty
        })
        @DisplayName("Should throw BusinessRuleValidationException when DNI format is invalid")
        void shouldThrowWhenDniIsInvalid(String invalidDni) {
            assertThatThrownBy(() -> TaxId.dni(invalidDni))
                    .isInstanceOf(BusinessRuleValidationException.class)
                    .satisfies(ex -> {
                        BusinessRuleValidationException bre = (BusinessRuleValidationException) ex;
                        assertThat(bre.errorCode()).isEqualTo("INVALID_DNI_FORMAT");
                        assertThat(bre.getMessage()).isEqualTo("National Identity Card (DNI) must have exactly 8 numeric digits: " + invalidDni.trim());
                    });
        }

        @ParameterizedTest(name = "Valid RUC with prefix {0}: {1}")
        @CsvSource({
                "10, 10456789019",
                "15, 15456789011",
                "17, 17456789013",
                "20, 20100070970",
                "20, 20456789014"
        })
        @DisplayName("Should validate valid 11-digit RUC with Módulo 11 checksum across all allowed prefixes (10, 15, 17, 20)")
        void shouldValidateValidRucModulo11(String prefix, String validRuc) {
            TaxId ruc = TaxId.ruc(validRuc);
            assertThat(ruc.value()).isEqualTo(validRuc);
            assertThat(ruc.type()).isEqualTo(TaxIdType.RUC);
            assertThat(ruc.value()).startsWith(prefix);

            // Trimming check
            TaxId trimmedRuc = TaxId.ruc("  " + validRuc + "  ");
            assertThat(trimmedRuc.value()).isEqualTo(validRuc);
        }

        @ParameterizedTest(name = "Invalid RUC Checksum: {0}")
        @ValueSource(strings = {
                "20100070971", // Corrupted check digit for SUNAT RUC (expected 0)
                "10456789010", // Corrupted check digit for prefix 10 (expected 9)
                "15456789012", // Corrupted check digit for prefix 15 (expected 1)
                "17456789010"  // Corrupted check digit for prefix 17 (expected 3)
        })
        @DisplayName("Should throw BusinessRuleValidationException when RUC checksum is mathematically invalid")
        void shouldThrowWhenRucChecksumIsInvalid(String invalidChecksumRuc) {
            assertThatThrownBy(() -> TaxId.ruc(invalidChecksumRuc))
                    .isInstanceOf(BusinessRuleValidationException.class)
                    .satisfies(ex -> {
                        BusinessRuleValidationException bre = (BusinessRuleValidationException) ex;
                        assertThat(bre.errorCode()).isEqualTo("INVALID_RUC_CHECKSUM");
                        assertThat(bre.getMessage()).isEqualTo("RUC verification check digit is not mathematically valid: " + invalidChecksumRuc);
                    });
        }

        @ParameterizedTest(name = "Invalid RUC Prefix: {0}")
        @ValueSource(strings = {
                "30100070970", // Prefix 30 not allowed
                "11456789019", // Prefix 11 not allowed
                "16456789019", // Prefix 16 not allowed
                "21456789014"  // Prefix 21 not allowed
        })
        @DisplayName("Should throw BusinessRuleValidationException when RUC prefix is invalid")
        void shouldThrowWhenRucPrefixIsInvalid(String invalidPrefixRuc) {
            assertThatThrownBy(() -> TaxId.ruc(invalidPrefixRuc))
                    .isInstanceOf(BusinessRuleValidationException.class)
                    .satisfies(ex -> {
                        BusinessRuleValidationException bre = (BusinessRuleValidationException) ex;
                        assertThat(bre.errorCode()).isEqualTo("INVALID_RUC_FORMAT");
                        assertThat(bre.getMessage()).isEqualTo("Tax ID (RUC) must have 11 digits and start with 10, 15, 17, or 20: " + invalidPrefixRuc);
                    });
        }

        @ParameterizedTest(name = "Invalid RUC Length: {0}")
        @ValueSource(strings = {
                "2010007097",    // 10 digits
                "201000709701",  // 12 digits
                "2010007097A",   // 11 chars with letter
                ""               // empty
        })
        @DisplayName("Should throw BusinessRuleValidationException when RUC length or characters are invalid")
        void shouldThrowWhenRucLengthIsInvalid(String malformedRuc) {
            assertThatThrownBy(() -> TaxId.ruc(malformedRuc))
                    .isInstanceOf(BusinessRuleValidationException.class)
                    .satisfies(ex -> {
                        BusinessRuleValidationException bre = (BusinessRuleValidationException) ex;
                        assertThat(bre.errorCode()).isEqualTo("INVALID_RUC_FORMAT");
                    });
        }

        @Test
        @DisplayName("Should throw NullPointerException when value or type is null")
        void shouldThrowWhenTaxIdInputIsNull() {
            assertThatThrownBy(() -> TaxId.ruc(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Tax document value cannot be null");

            assertThatThrownBy(() -> TaxId.dni(null))
                    .isInstanceOf(NullPointerException.class);

            assertThatThrownBy(() -> new TaxId("12345678", null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Tax document type cannot be null");
        }

        @Test
        @DisplayName("Should expose TaxIdType descriptions and expected lengths")
        void shouldExposeTaxIdTypeAttributes() {
            assertThat(TaxIdType.RUC.description()).isEqualTo("Unique Taxpayer Registry");
            assertThat(TaxIdType.RUC.expectedLength()).isEqualTo(11);

            assertThat(TaxIdType.DNI.description()).isEqualTo("National Identity Document");
            assertThat(TaxIdType.DNI.expectedLength()).isEqualTo(8);

            assertThat(TaxIdType.CE.description()).isEqualTo("Foreigner Identity Card");
            assertThat(TaxIdType.CE.expectedLength()).isEqualTo(12);

            assertThat(TaxIdType.PASSPORT.description()).isEqualTo("International Passport");
            assertThat(TaxIdType.PASSPORT.expectedLength()).isEqualTo(12);
        }
    }

    @Nested
    @DisplayName("Email, Phone & DateRange Tests")
    class ContactAndTemporalTests {

        @Test
        @DisplayName("Should validate, trim, and normalize email to lowercase")
        void shouldValidateAndNormalizeEmail() {
            EmailAddress email = EmailAddress.of("user@atelier.com");
            assertThat(email.value()).isEqualTo("user@atelier.com");

            // Subdomain and plus tag support
            EmailAddress tagged = EmailAddress.of("john.doe+workshop@dev.atelier.pe");
            assertThat(tagged.value()).isEqualTo("john.doe+workshop@dev.atelier.pe");

            // Trimming and case normalization to lowercase
            EmailAddress upper = EmailAddress.of("  User.Name@Atelier.COM  ");
            assertThat(upper.value()).isEqualTo("user.name@atelier.com");
        }

        @ParameterizedTest(name = "Invalid email: {0}")
        @ValueSource(strings = {
                "invalid-email",
                "user@",
                "@atelier.com",
                "user@domain",
                "user@domain.c",
                "user name@atelier.com",
                "user@@atelier.com",
                ""
        })
        @DisplayName("Should throw BusinessRuleValidationException when email format violates RFC 5322 pattern")
        void shouldThrowWhenEmailFormatIsInvalid(String invalidEmail) {
            assertThatThrownBy(() -> EmailAddress.of(invalidEmail))
                    .isInstanceOf(BusinessRuleValidationException.class)
                    .satisfies(ex -> {
                        BusinessRuleValidationException bre = (BusinessRuleValidationException) ex;
                        assertThat(bre.errorCode()).isEqualTo("INVALID_EMAIL_FORMAT");
                    });
        }

        @Test
        @DisplayName("Should throw NullPointerException when email is null")
        void shouldThrowWhenEmailIsNull() {
            assertThatThrownBy(() -> EmailAddress.of(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Email address cannot be null");
        }

        @Test
        @DisplayName("Should validate phone number conforming to E.164 and strip whitespaces")
        void shouldValidateAndNormalizePhone() {
            // Standard international with plus
            PhoneNumber phoneWithPlus = PhoneNumber.of("+51987654321");
            assertThat(phoneWithPlus.value()).isEqualTo("+51987654321");

            // National number without plus (8 digits)
            PhoneNumber nationalPhone = PhoneNumber.of("987654321");
            assertThat(nationalPhone.value()).isEqualTo("987654321");

            // Boundary: minimum 8 digits
            PhoneNumber minPhone = PhoneNumber.of("+12345678");
            assertThat(minPhone.value()).isEqualTo("+12345678");

            // Boundary: maximum 15 digits
            PhoneNumber maxPhone = PhoneNumber.of("+123456789012345");
            assertThat(maxPhone.value()).isEqualTo("+123456789012345");

            // Whitespace removal (internal spaces, tabs, surrounding spaces)
            PhoneNumber phoneWithSpaces = PhoneNumber.of("  +51  987  654  321  ");
            assertThat(phoneWithSpaces.value()).isEqualTo("+51987654321");
        }

        @ParameterizedTest(name = "Invalid phone number: {0}")
        @ValueSource(strings = {
                "123",               // too short (< 8 digits)
                "+1234567",          // 7 digits with plus
                "1234567",           // 7 digits
                "+1234567890123456", // 16 digits (exceeds max 15 digits)
                "+5198765432A",      // contains alphanumeric
                "+51-987-654-321",   // contains hyphen delimiters
                ""                   // empty
        })
        @DisplayName("Should throw BusinessRuleValidationException when phone violates E.164 standard")
        void shouldThrowWhenPhoneNumberIsInvalid(String invalidPhone) {
            assertThatThrownBy(() -> PhoneNumber.of(invalidPhone))
                    .isInstanceOf(BusinessRuleValidationException.class)
                    .satisfies(ex -> {
                        BusinessRuleValidationException bre = (BusinessRuleValidationException) ex;
                        assertThat(bre.errorCode()).isEqualTo("INVALID_PHONE_FORMAT");
                    });
        }

        @Test
        @DisplayName("Should throw NullPointerException when phone is null")
        void shouldThrowWhenPhoneIsNull() {
            assertThatThrownBy(() -> PhoneNumber.of(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Phone number cannot be null");
        }

        @Test
        @DisplayName("Should validate DateRange and check containment with boundary dates")
        void shouldValidateDateRangeAndContainment() {
            LocalDate start = LocalDate.of(2026, 1, 10);
            LocalDate end = LocalDate.of(2026, 1, 20);
            DateRange range = DateRange.of(start, end);

            // Single day range (same start and end)
            DateRange singleDayRange = DateRange.of(start, start);
            assertThat(singleDayRange.contains(start)).isTrue();

            // Boundary checks
            assertThat(range.contains(start)).isTrue();
            assertThat(range.contains(end)).isTrue();
            assertThat(range.contains(LocalDate.of(2026, 1, 15))).isTrue();
            assertThat(range.contains(LocalDate.of(2026, 1, 9))).isFalse();
            assertThat(range.contains(LocalDate.of(2026, 1, 21))).isFalse();

            // Null containment check throws NPE
            assertThatThrownBy(() -> range.contains(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Date to check cannot be null");
        }

        @Test
        @DisplayName("Should verify DateRange overlaps scenarios")
        void shouldVerifyDateRangeOverlapsScenarios() {
            DateRange baseRange = DateRange.of(LocalDate.of(2026, 1, 10), LocalDate.of(2026, 1, 20));

            // Touching at end boundary
            DateRange touchingEnd = DateRange.of(LocalDate.of(2026, 1, 20), LocalDate.of(2026, 1, 30));
            assertThat(baseRange.overlaps(touchingEnd)).isTrue();

            // Touching at start boundary
            DateRange touchingStart = DateRange.of(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 10));
            assertThat(baseRange.overlaps(touchingStart)).isTrue();

            // Partial overlap
            DateRange partialOverlap = DateRange.of(LocalDate.of(2026, 1, 15), LocalDate.of(2026, 1, 25));
            assertThat(baseRange.overlaps(partialOverlap)).isTrue();

            // Sub-range enclosed
            DateRange enclosed = DateRange.of(LocalDate.of(2026, 1, 12), LocalDate.of(2026, 1, 18));
            assertThat(baseRange.overlaps(enclosed)).isTrue();

            // Encompassing super-range
            DateRange encompassing = DateRange.of(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));
            assertThat(baseRange.overlaps(encompassing)).isTrue();

            // Identical range
            DateRange identical = DateRange.of(LocalDate.of(2026, 1, 10), LocalDate.of(2026, 1, 20));
            assertThat(baseRange.overlaps(identical)).isTrue();

            // Disjoint before
            DateRange disjointBefore = DateRange.of(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 9));
            assertThat(baseRange.overlaps(disjointBefore)).isFalse();

            // Disjoint after
            DateRange disjointAfter = DateRange.of(LocalDate.of(2026, 1, 21), LocalDate.of(2026, 1, 31));
            assertThat(baseRange.overlaps(disjointAfter)).isFalse();

            // Null check
            assertThatThrownBy(() -> baseRange.overlaps(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Date range to compare cannot be null");
        }

        @Test
        @DisplayName("Should throw BusinessRuleValidationException when DateRange end is before start")
        void shouldThrowWhenDateRangeEndIsBeforeStart() {
            LocalDate start = LocalDate.of(2026, 1, 31);
            LocalDate end = LocalDate.of(2026, 1, 1);
            assertThatThrownBy(() -> DateRange.of(start, end))
                    .isInstanceOf(BusinessRuleValidationException.class)
                    .satisfies(ex -> {
                        BusinessRuleValidationException bre = (BusinessRuleValidationException) ex;
                        assertThat(bre.errorCode()).isEqualTo("INVALID_DATE_RANGE");
                    });
        }

        @Test
        @DisplayName("Should throw NullPointerException when DateRange dates are null")
        void shouldThrowWhenDateRangeDatesAreNull() {
            LocalDate now = LocalDate.now();
            assertThatThrownBy(() -> DateRange.of(null, now))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Start date cannot be null");

            assertThatThrownBy(() -> DateRange.of(now, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("End date cannot be null");
        }
    }
}
