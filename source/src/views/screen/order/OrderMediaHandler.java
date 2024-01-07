package views.screen.order;
import entity.order.Order;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import utils.Configs;
import utils.Utils;
import utils.enums.OrderStatus;
import utils.resources.Resource;
import views.screen.BaseScreenHandler;
import views.screen.FXMLScreenHandler;
import views.screen.payment.ResultScreenHandler;

import java.io.File;
import java.io.IOException;

public class OrderMediaHandler extends FXMLScreenHandler {
    @FXML
    private Label addess;

    @FXML
    private Button cancelBtn;

    @FXML
    private Label createdDate;

    @FXML
    private Label currency;

    @FXML
    private HBox hboxMedia;

    @FXML
    private ImageView image;

    @FXML
    private VBox imageLogoVbox;

    @FXML
    private Label name;

    @FXML
    private Label phone;

    @FXML
    private Label price;

    @FXML
    private Label shippingFees;

    @FXML
    private Label status;

    @FXML
    private VBox vBoxInfo;

    @FXML
    private VBox vBoxPrice;

    @FXML
    private VBox vboxBtns;

    private OrderScreenHandler orderMediaHandler;
    private Order order;
    public OrderMediaHandler(String screenPath, OrderScreenHandler orderScreenHandler) throws IOException {
        super(screenPath);
        this.orderMediaHandler = orderScreenHandler;


    }

    public void setOrder(Order order) {
        this.order = order;
        File file = new File("assets/images/Logo.png");
        Image im = new Image(file.toURI().toString());
        image.setImage(im);
        setOrderInfo();
    }

    private void setOrderInfo(){
        Resource resource = new Resource();
        name.setText("Name: " + this.order.getName());
        addess.setText("Address: " + this.order.getAddress());
        phone.setText("Phone: " + this.order.getPhoneNumber());
        price.setText("Price: " + Utils.getCurrencyFormat(this.order.getPaymentTransaction().getAmount() / 1000));
        shippingFees.setText("Shipping fees: " + Utils.getCurrencyFormat(this.order.getShippingFees() ));

        status.setText( "Status: " + resource.orderStatusStringHashMap.get(order.getStatus()));
        createdDate.setText( "Created At: " + Utils.formatDateTime(this.order.getPaymentTransaction().getCreatedAt(), "dd/MM/yyyy HH:mm:ss"));

        displayItemsNeed();

    }
    private void displayItemsNeed(){
        if(this.order.getStatus().ordinal() == OrderStatus.Rejected.ordinal()){
            cancelBtn.setVisible(false);
//            cancelBtn.setManaged(false);
            return;
        }
        cancelBtn.setOnMouseClicked( e -> handleRejectedOrder());
    }
    private void handleRejectedOrder(){

        try {
            orderMediaHandler.handleRejectOrder(order);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
