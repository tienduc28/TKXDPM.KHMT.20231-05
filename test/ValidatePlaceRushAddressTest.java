import controller.PlaceOrderController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ValidatePlaceRushAddressTest {

    private PlaceOrderController placeOrderController;


    /**
     * @throws Exception
     */
    @BeforeEach
    void setUp() throws Exception {
        placeOrderController = new PlaceOrderController();
    }

    @ParameterizedTest
    @CsvSource({
            "Hà Nội, any, true",
            "Hà fs, b, false",
    })

    public void test(String province, String address, boolean expected) {
        boolean isValid = placeOrderController.validateAddressPlaceRushOrder(province, address);
        assertEquals(expected, isValid);
    }
}
