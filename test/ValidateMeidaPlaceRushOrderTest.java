import controller.PlaceOrderController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ValidateMeidaPlaceRushOrderTest {

    private PlaceOrderController placeOrderController;

    @BeforeEach
    void setup() throws Exception {
        placeOrderController = new PlaceOrderController();
    }


    @ParameterizedTest
    public void test() {
        boolean isValid = placeOrderController.validateMediaPlaceRushorder();
        assertEquals(true, isValid);
    }
}
