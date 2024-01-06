package views.screen.home;

import common.exception.ViewCartException;
import controller.AccountController;
import controller.HomeController;
import controller.OrderController;
import controller.ViewCartController;
import entity.cart.Cart;
import entity.media.Media;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import utils.Configs;
import utils.Utils;
import views.screen.BaseScreenHandler;
import views.screen.cart.CartScreenHandler;
import views.screen.login.LoginScreenHandler;
import views.screen.order.OrderScreenHandler;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class HomeScreenHandler extends BaseScreenHandler implements Initializable {

    public static Logger LOGGER = Utils.getLogger(HomeScreenHandler.class.getName());

    @FXML
    private Label numMediaInCart;

    @FXML
    private Button login;

    @FXML
    private Button orderBtn;

    @FXML
    private ImageView aimsImage;

    @FXML
    private ImageView cartImage;

    @FXML
    private VBox vboxMedia1;

    @FXML
    private VBox vboxMedia2;

    @FXML
    private VBox vboxMedia3;

    @FXML
    private HBox hboxMedia;

    @FXML
    private SplitMenuButton splitMenuBtnSearch;

    private List homeItems;

    public HomeScreenHandler(Stage stage, String screenPath) throws IOException {
        super(stage, screenPath);
    }

    /**
     * @return Label
     */
//Functional Cohesion
    public Label getNumMediaCartLabel() {
        return this.numMediaInCart;
    }

    /**
     * @return HomeController
     */
//Functional Cohesion
    public HomeController getBController() {
        return (HomeController) super.getBController();
    }

    @Override
//Sequential Cohesion
    public void show() {
        try {
            if (accountController.getLoggedInAccount() != null) {
                login.setText("Chào mừng, " + accountController.getLoggedInAccount().getName());
            }
        } catch(Exception e) {
            System.out.println("null");
        }
        numMediaInCart.setText(String.valueOf(Cart.getCart().getListMedia().size()) + " media");
        super.show();
    }

    /**
     * @param arg0
     * @param arg1
     */
    @Override
// Control Coupling
// Control Cohesion
    public void initialize(URL arg0, ResourceBundle arg1) {
        setBController(new HomeController());
        try {
            List medium = getBController().getAllMedia();
            this.homeItems = new ArrayList<>();
            for (Object object : medium) {
                Media media = (Media) object;
                MediaHandler m1 = new MediaHandler(Configs.HOME_MEDIA_PATH, media, this);
                this.homeItems.add(m1);
            }
        } catch (SQLException | IOException e) {
            LOGGER.info("Errors occured: " + e.getMessage());
            e.printStackTrace();
        }

        login.setOnMouseClicked(e -> {
            LoginScreenHandler loginHandler;
            try {
                loginHandler = new LoginScreenHandler(stage, Configs.LOGIN_PATH);
                loginHandler.setHomeScreenHandler(this);
                loginHandler.setScreenTitle("Login");
                accountController = loginHandler.getBController();
                loginHandler.show();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });

        aimsImage.setOnMouseClicked(e -> {
            addMediaHome(this.homeItems);
        });

        cartImage.setOnMouseClicked(e -> {

            try {
                var cartScreen = new CartScreenHandler(this.stage, Configs.CART_SCREEN_PATH);
                cartScreen.setHomeScreenHandler(this);
                cartScreen.setBController(new ViewCartController());
                cartScreen.show(this);
            } catch (IOException | SQLException e1) {
                throw new ViewCartException(Arrays.toString(e1.getStackTrace()).replaceAll(", ", "\n"));
            }
        });

        orderBtn.setOnMouseClicked(e -> {
            try {
                var orderScreen = new OrderScreenHandler(this.stage, Configs.ORDER_PATH);
                orderScreen.setHomeScreenHandler(this);
                orderScreen.setBController(new OrderController());
                orderScreen.show(this);
            } catch (IOException | SQLException e1) {
                e1.printStackTrace();
            }
        });

        addMediaHome(this.homeItems);
        addMenuItem(0, "Book", splitMenuBtnSearch);
        addMenuItem(1, "DVD", splitMenuBtnSearch);
        addMenuItem(2, "CD", splitMenuBtnSearch);
    }
//Data Coupling 
//Data Cohesion
    public void setImage() {
        // fix image path caused by fxml
        File file1 = new File(Configs.IMAGE_PATH + "/" + "Logo.png");
        Image img1 = new Image(file1.toURI().toString());
        aimsImage.setImage(img1);

        File file2 = new File(Configs.IMAGE_PATH + "/" + "cart.png");
        Image img2 = new Image(file2.toURI().toString());
        cartImage.setImage(img2);
    }

    /**
     * @param items
     */
//Data Coupling 
//Data Cohesion
    public void addMediaHome(List items) {
        ArrayList mediaItems = (ArrayList) ((ArrayList) items).clone();
        hboxMedia.getChildren().forEach(node -> {
            VBox vBox = (VBox) node;
            vBox.getChildren().clear();
        });
        while (!mediaItems.isEmpty()) {
            hboxMedia.getChildren().forEach(node -> {
                int vid = hboxMedia.getChildren().indexOf(node);
                VBox vBox = (VBox) node;
                while (vBox.getChildren().size() < 3 && !mediaItems.isEmpty()) {
                    MediaHandler media = (MediaHandler) mediaItems.get(0);
                    vBox.getChildren().add(media.getContent());
                    mediaItems.remove(media);
                }
            });
            return;
        }
    }

    /**
     * @param position
     * @param text
     * @param menuButton
     */

// Data Coupling 
// Data Cohesion
    private void addMenuItem(int position, String text, MenuButton menuButton) {
        MenuItem menuItem = new MenuItem();
        Label label = new Label();
        label.prefWidthProperty().bind(menuButton.widthProperty().subtract(31));
        label.setText(text);
        label.setTextAlignment(TextAlignment.RIGHT);
        menuItem.setGraphic(label);
        menuItem.setOnAction(e -> {
            // empty home media
            hboxMedia.getChildren().forEach(node -> {
                VBox vBox = (VBox) node;
                vBox.getChildren().clear();
            });

            // filter only media with the choosen category
            List filteredItems = new ArrayList<>();
            homeItems.forEach(me -> {
                MediaHandler media = (MediaHandler) me;
                if (media.getMedia().getTitle().toLowerCase().startsWith(text.toLowerCase())) {
                    filteredItems.add(media);
                }
            });

            // fill out the home with filted media as category
            addMediaHome(filteredItems);
        });
        menuButton.getItems().add(position, menuItem);
    }

}
