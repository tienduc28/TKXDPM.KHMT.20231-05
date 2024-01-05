package views.screen.cart;

import common.exception.MediaNotAvailableException;
import common.exception.PlaceOrderException;
import controller.PlaceOrderController;
import controller.ViewCartController;
import entity.cart.CartMedia;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import utils.Configs;
import utils.Utils;
import views.screen.BaseScreenHandler;
import views.screen.popup.PopupScreen;
import views.screen.shipping.ShippingScreenHandler;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

//SRP: Do CartScreenHandler dam nhan nhieu trach nhiem dong thoi xu ly cac thao tac UI.
//DIP: CartScreenHandler nen phu thuoc vao cac giao dien thay vi cac lop truc tiep.

public class CartScreenHandler extends BaseScreenHandler {

    private static Logger LOGGER = Utils.getLogger(CartScreenHandler.class.getName());
    @FXML
    VBox vboxCart;
    @FXML
    private ImageView aimsImage;
    @FXML
    private Label pageTitle;
    @FXML
    private Label shippingFees;

    @FXML
    private Label labelAmount;

    @FXML
    private Label labelSubtotal;

    @FXML
    private Label labelVAT;

    @FXML
    private Button btnPlaceOrder;

    //Control Coupling
    //Functional Cohesion
    public CartScreenHandler(Stage stage, String screenPath) throws IOException {
        super(stage, screenPath);

        // fix relative image path caused by fxml
        File file = new File("assets/images/Logo.png");
        Image im = new Image(file.toURI().toString());
        aimsImage.setImage(im);

        // on mouse clicked, we back to home
        aimsImage.setOnMouseClicked(e -> {
            homeScreenHandler.show();
        });

        // on mouse clicked, we start processing place order usecase
        btnPlaceOrder.setOnMouseClicked(e -> {

            try {
                requestOrder();
            } catch (SQLException | IOException exp) {

                exp.printStackTrace();
                throw new PlaceOrderException(Arrays.toString(exp.getStackTrace()).replaceAll(", ", "\n"));
            }

        });
    }


    //Khong xac dinh coupling
    //Functional Cohesion
    /**
     * @return Label
     */
    public Label getLabelAmount() {
        return labelAmount;
    }


    //Khong xac dinh coupling
    //Functional Cohesion
    /**
     * @return Label
     */
    public Label getLabelSubtotal() {
        return labelSubtotal;
    }


    //Khong xac dinh coupling
    //Sequential Cohesion
    /**
     * @return ViewCartController
     */
    public ViewCartController getBController() {
        return (ViewCartController) super.getBController();
    }


    //Khong xac dinh coupling
    //Functional Cohesion
    /**
     * @param prevScreen
     * @throws SQLException
     */
    public void show(BaseScreenHandler prevScreen) throws SQLException {
        setPreviousScreen(prevScreen);
        setScreenTitle("Cart Screen");
        getBController().checkAvailabilityOfProduct();
        displayCartWithMediaAvailability();
        show();
    }


    //Control coupling
    //Procedural Cohesion
    /**
     * @throws SQLException
     * @throws IOException
     */
    public void requestOrder() throws SQLException, IOException {
        try {
            // create placeOrderController and process the order
            var placeOrderController = new PlaceOrderController();
            if (placeOrderController.getListCartMedia().size() == 0) {
                PopupScreen.error("You don't have anything to place");
                return;
            }

            placeOrderController.placeOrder();

            // display available media
            displayCartWithMediaAvailability();

            // create order
            var order = placeOrderController.createOrder();

            // display shipping form
            ShippingScreenHandler ShippingScreenHandler = new ShippingScreenHandler(this.stage, Configs.SHIPPING_SCREEN_PATH, order);
            ShippingScreenHandler.setPreviousScreen(this);
            ShippingScreenHandler.setHomeScreenHandler(homeScreenHandler);
            ShippingScreenHandler.setScreenTitle("Shipping Screen");
            ShippingScreenHandler.setBController(placeOrderController);
            ShippingScreenHandler.show();

        } catch (MediaNotAvailableException e) {
            // if some media are not available then display cart and break usecase Place Order
            displayCartWithMediaAvailability();
        }
    }

    //Khong xac dinh coupling
    /**
     * @throws SQLException
     */
    public void updateCart() throws SQLException {
        getBController().checkAvailabilityOfProduct();
        displayCartWithMediaAvailability();
    }

    //Khong xac dinh coupling
    //Functional Cohesion
    void updateCartAmount() {
        // calculate subtotal and amount
        int subtotal = getBController().getCartSubtotal();
        int vat = (int) ((Configs.PERCENT_VAT / 100) * subtotal);
        int amount = subtotal + vat;


        // update subtotal and amount of Cart
        labelSubtotal.setText(Utils.getCurrencyFormat(subtotal));
        labelVAT.setText(Utils.getCurrencyFormat(vat));
        labelAmount.setText(Utils.getCurrencyFormat(amount));
    }
    //Control Coupling
    //Functional Cohesion
    private void displayCartWithMediaAvailability() {
        // clear all old cartMedia
        vboxCart.getChildren().clear();

        // get list media of cart after check availability
        List lstMedia = getBController().getListCartMedia();

        try {
            for (Object cm : lstMedia) {

                // display the attribute of vboxCart media
                CartMedia cartMedia = (CartMedia) cm;
                MediaHandler mediaCartScreen = new MediaHandler(Configs.CART_MEDIA_PATH, this);
                mediaCartScreen.setCartMedia(cartMedia);

                // add spinner
                vboxCart.getChildren().add(mediaCartScreen.getContent());
            }
            // calculate subtotal and amount
            updateCartAmount();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
