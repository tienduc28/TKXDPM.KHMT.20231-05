package subsystem.vnPay;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import common.exception.*;
import entity.order.entities.RefundTransaction;
import entity.payment.PaymentTransaction;
import subsystem.VnPayInterface;
import com.google.gson.JsonObject;
import entity.order.entities.RefundResponse;
import common.exception.vnPayException.PaymentExceptionHolder;

import java.io.*;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 */
public class VnPaySubsystemController implements VnPayInterface {

    private final String PAY_COMMAND = "pay";
    private final String VERSION = "2.1.0";


   /**
    * Data coupling
    * Functional cohesion
    * @param money
    * @param contents
    */
    @Override
    public String generatePayUrl(int money, String contents) throws IOException {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String orderType = "other";
        long amount = money * 100L * 1000;


        String vnp_TxnRef = Config.getRandomNumber(8);
        String vnp_IpAddr = Config.getIpAddress();

        String vnp_TmnCode = Config.vnp_TmnCode;

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");

        vnp_Params.put("vnp_BankCode", "");

        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", contents);
        vnp_Params.put("vnp_OrderType", orderType);


        vnp_Params.put("vnp_Locale", "vn");

        vnp_Params.put("vnp_ReturnUrl", Config.vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List fieldNames = new ArrayList(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                //Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                //Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = Config.hmacSHA512(Config.secretKey, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        return Config.vnp_PayUrl + "?" + queryUrl;
    }

    /**
     * Data coupling
     * Functional Cohesion
     * @param response
     * @return PaymentTransaction
     */
    public PaymentTransaction makePaymentTransaction(Map<String, String> response) throws ParseException {
        if (response == null) {
            return null;
        }

        // Create Payment transaction
        String errorCode = response.get("vnp_TransactionStatus");
        String cardType = response.get("vnp_CardType");

        String txnRef = response.get("vnp_TxnRef");
        String transactionId = response.get("vnp_TransactionNo");
        String transactionContent = response.get("vnp_OrderInfo");
        int amount = Integer.parseInt((String) response.get("vnp_Amount")) / 100;
        String createdAt = response.get("vnp_PayDate");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = dateFormat.parse(createdAt);
        PaymentTransaction trans = new
                PaymentTransaction(errorCode, transactionId, transactionContent, amount, date, cardType, txnRef);
            return trans;
    }

    public RefundResponse refund(RefundTransaction refundTransaction) throws  IOException, PaymentException {

        //Command: refund
        String vnp_RequestId = Config.getRandomNumber(8);
        String vnp_Version = "2.1.0";
        String vnp_Command = "refund";
        String vnp_TmnCode = Config.vnp_TmnCode;
        String vnp_TransactionType = refundTransaction.getTrantype(); // 02 or 03
        String vnp_TxnRef = refundTransaction.getOrder_id();

        long amount = Long.parseLong(refundTransaction.getAmount())*100;
        String vnp_Amount = String.valueOf(amount);
        String vnp_OrderInfo = "Hoan tien GD OrderId:" + vnp_TxnRef;

        String vnp_TransactionNo = refundTransaction.getTransactionNo();
        String vnp_TransactionDate = refundTransaction.getTrans_date();
        String vnp_CreateBy = refundTransaction.getUser();

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());

        String vnp_IpAddr = Config.getIpAddress();

        JsonObject vnp_Params =new JsonObject ();

        vnp_Params.addProperty("vnp_RequestId", vnp_RequestId);
        vnp_Params.addProperty("vnp_Version", vnp_Version);
        vnp_Params.addProperty("vnp_Command", vnp_Command);
        vnp_Params.addProperty("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.addProperty("vnp_TransactionType", vnp_TransactionType);
        vnp_Params.addProperty("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.addProperty("vnp_Amount", vnp_Amount);
        vnp_Params.addProperty("vnp_OrderInfo", vnp_OrderInfo);

        if(vnp_TransactionNo != null && !vnp_TransactionNo.isEmpty())
        {
            vnp_Params.addProperty("vnp_TransactionNo", vnp_TransactionNo);
        }

        vnp_Params.addProperty("vnp_TransactionDate", vnp_TransactionDate);
        vnp_Params.addProperty("vnp_CreateBy", vnp_CreateBy);
        vnp_Params.addProperty("vnp_CreateDate", vnp_CreateDate);
        vnp_Params.addProperty("vnp_IpAddr", vnp_IpAddr);

        String hash_Data= String.join("|", vnp_RequestId, vnp_Version, vnp_Command, vnp_TmnCode,
                vnp_TransactionType, vnp_TxnRef, vnp_Amount, vnp_TransactionNo, vnp_TransactionDate,
                vnp_CreateBy, vnp_CreateDate, vnp_IpAddr, vnp_OrderInfo);

        String vnp_SecureHash = Config.hmacSHA512(Config.secretKey, hash_Data.toString());

        vnp_Params.addProperty("vnp_SecureHash", vnp_SecureHash);

        URL url = new URL (Config.vnp_ApiUrl);
        var con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("POST");

        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(vnp_Params.toString());
        wr.flush();
        wr.close();
        int responseCode = con.getResponseCode();

        BufferedReader in = new BufferedReader(
        new InputStreamReader(con.getInputStream()));
        String output;
        StringBuffer response = new StringBuffer();
        while ((output = in.readLine()) != null) {
        response.append(output);
        }
        in.close();
        return handleRefundResponse(response.toString());

    }

    private RefundResponse handleRefundResponse(String response) throws PaymentException {
        Type type = new TypeToken<Map<String, String>>(){}.getType();
        Map<String, String> resultMap = new Gson().fromJson(response, type);

        var trans = new RefundResponse();
        trans.setErrorCode(resultMap.get("vnp_TransactionStatus"));
        trans.setAmount((int) (Long.parseLong(resultMap.get("vnp_Amount")) / 100));
        trans.setTransactionContent(resultMap.get("vnp_OrderInfo"));
        String createdAt = resultMap.get("vnp_PayDate");

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

        Date date = null;
        try {
            date = dateFormat.parse(createdAt);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        trans.setCreatedAt(date);
        trans.setTransactionContent(resultMap.get("vnp_TransactionNo"));
        trans.setTxnRef(resultMap.get("vnp_TxnRef"));
        var ex = PaymentExceptionHolder.getInstance().getException(trans.getErrorCode());
        if(ex != null){
            throw ex;
        }
        return trans;
    }

}
