package entity.order;

import entity.db.AIMSDB;
import entity.payment.PaymentTransaction;
import entity.shipping.Shipment;
import utils.Configs;
import utils.enums.OrderStatus;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Order {


    private String phone;
    private int id;
    private int shippingFees;
    private List<OrderMedia> lstOrderMedia;
    private Shipment shipment;
    private String name;
    private String province;
    private String instruction;
    private OrderStatus status;
    private PaymentTransaction paymentTransaction;



    public PaymentTransaction getPaymentTransaction() {
        return paymentTransaction;
    }

    public OrderStatus getStatus(){
        return status;
    }

    public void setStatus(OrderStatus stt){
        status = stt;
    }
    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    private String address;

    public List<OrderMedia> getLstOrderMedia() {
        return lstOrderMedia;
    }

    public void setLstOrderMedia(List<OrderMedia> lstOrderMedia) {
        this.lstOrderMedia = lstOrderMedia;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public Order() {
        this.lstOrderMedia = new ArrayList<OrderMedia>();
    }

    public Order(List lstOrderMedia) {
        this.lstOrderMedia = lstOrderMedia;
    }

    public List<Order> getListOrders(){
        List<Order> orders = new ArrayList<>();

        try {
            var query = "SELECT o.id, o.name, o.province, o.address, o.phone, o.shippingFees,  o.status, " +
                    "s.shipType, s.deliveryInstruction, s.deliveryTime, s.shipmentDetail, " +
                    "p.transactionNo, p.txnRef, p.cardType, p.amount, p.createdAt, p.content " +
                    "FROM `Order` o " +
                    "LEFT JOIN Shipment s ON s.orderId = o.id " +
                    "LEFT JOIN PaymentTransaction p ON o.id = p.orderID ORDER BY p.createdAt DESC";
            PreparedStatement preparedStatement = AIMSDB.getConnection().prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            HashMap<Integer, Order> orderMap = new HashMap<>();

            while (resultSet.next()) {
                int orderId = resultSet.getInt("id");
                Order order = orderMap.get(orderId);

                if (order == null) {
                    order = new Order();
                    order.setId(orderId);
                    order.setName(resultSet.getString("name"));
                    order.setProvince(resultSet.getString("province"));
                    order.setAddress(resultSet.getString("address"));
                    order.setPhone(resultSet.getString("phone"));
                    order.setShippingFees(resultSet.getInt("shippingFees"));
                    order.setStatus(OrderStatus.values()[resultSet.getInt("status")]);


                    var shipment = new Shipment();
                    shipment.setShipType(resultSet.getInt("shipType"));
                    shipment.setDeliveryInstruction(resultSet.getString("deliveryInstruction"));
                    shipment.setDeliveryTime(resultSet.getString("deliveryTime"));
                    shipment.setShipmentDetail(resultSet.getString("shipmentDetail"));

                    order.setShipment(shipment);

                    var paymentTransaction = new PaymentTransaction();
                    paymentTransaction.setTransactionNo(resultSet.getString(("transactionNo")));
                    paymentTransaction.setTxnRef(resultSet.getString(("txnRef")));
                    paymentTransaction.setCardType(resultSet.getString(("cardType")));
                    paymentTransaction.setAmount(resultSet.getInt(("amount")));
                    paymentTransaction.setCreatedAt(resultSet.getDate(("createdAt")));
                    paymentTransaction.setTransactionContent(resultSet.getString(("content")));



                    order.paymentTransaction = paymentTransaction;

                    orderMap.put(orderId, order);
                    orders.add(order);
                }


                OrderMedia orderMedia = new OrderMedia();  // Replace with actual logic
                order.addOrderMedia(orderMedia);
            }

            resultSet.close();
            preparedStatement.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return orders;
    }

    public void createOrderEntity(){
        try {
            Statement stm = AIMSDB.getConnection().createStatement();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        String query = "INSERT INTO 'Order' (name, province, address, phone, shippingFees) " +
                "VALUES ( ?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = AIMSDB.getConnection().prepareStatement(query)) {
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, province);
            preparedStatement.setString(3, address);
            preparedStatement.setString(4, phone);
            preparedStatement.setInt(5, shippingFees);



            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating order failed, no rows affected.");
            }

            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    id = generatedKeys.getInt(1);

                } else {
                    throw new SQLException("Creating order failed, no ID obtained.");
                }
            }
            shipment.setOrderId(id);
            shipment.save();
            lstOrderMedia.forEach(medio -> {
                medio.save(id);
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateStatus(OrderStatus status, int orderId) {
        try {
            String query = "UPDATE `Order` SET status = ? WHERE id = ?";
            try (PreparedStatement preparedStatement = AIMSDB.getConnection().prepareStatement(query)) {
                if (status != null) {
                    preparedStatement.setInt(1, status.ordinal());
                } else {
                    throw new IllegalArgumentException("Status cannot be null");
                }
                preparedStatement.setInt(2, orderId);

                int affectedRows = preparedStatement.executeUpdate();

                if (affectedRows == 0) {
                    throw new SQLException("Update order failed, no rows affected.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * @param om
     */
    public void addOrderMedia(OrderMedia om) {
        this.lstOrderMedia.add(om);
    }


    /**
     * @param om
     */
    public void removeOrderMedia(OrderMedia om) {
        this.lstOrderMedia.remove(om);
    }


    /**
     * @return List
     */
    public List<OrderMedia> getlstOrderMedia() {
        return this.lstOrderMedia;
    }


    /**
     * @param lstOrderMedia
     */
    public void setlstOrderMedia(List lstOrderMedia) {
        this.lstOrderMedia = lstOrderMedia;
    }

    /**
     * @return int
     */
    public int getShippingFees() {
        return shippingFees;
    }

    /**
     * @param shippingFees
     */
    public void setShippingFees(int shippingFees) {
        this.shippingFees = shippingFees;
    }

    public Shipment getShipment() {
        return shipment;
    }

    public void setShipment(Shipment shipment) {
        this.shipment = shipment;
    }

    /**
     * @return int
     */
    public int getAmount() {
        double amount = 0;
        for (Object object : lstOrderMedia) {
            OrderMedia om = (OrderMedia) object;
            amount += om.getPrice();
        }
        return (int) (amount + (Configs.PERCENT_VAT / 100) * amount);
    }

    public void setPaymentTransaction(PaymentTransaction paymentTransaction) {
    }
}
