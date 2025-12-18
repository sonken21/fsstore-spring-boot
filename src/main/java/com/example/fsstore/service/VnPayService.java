package com.example.fsstore.service;

import com.example.fsstore.config.VnPayConfig;
import com.example.fsstore.entity.Order;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class VnPayService {

    public String createPaymentUrl(Order order, HttpServletRequest request) {
        try {
            // 1. Tính toán số tiền (VNPAY nhân thêm 100)
            long amount = (long) (order.getOrderTotal().doubleValue() * 100);

            // 2. Lấy IP người dùng (Tránh dùng fix cứng 127.0.0.1)
            String vnp_IpAddr = request.getHeader("X-Forwarded-For");
            if (vnp_IpAddr == null || vnp_IpAddr.isEmpty()) {
                vnp_IpAddr = request.getRemoteAddr();
            }

            Map<String, String> vnp_Params = new TreeMap<>();
            vnp_Params.put("vnp_Version", "2.1.0");
            vnp_Params.put("vnp_Command", "pay");
            vnp_Params.put("vnp_TmnCode", VnPayConfig.vnp_TmnCode);
            vnp_Params.put("vnp_Amount", String.valueOf(amount));
            vnp_Params.put("vnp_CurrCode", "VND");
            vnp_Params.put("vnp_TxnRef", String.valueOf(order.getId()));
            // OrderInfo không nên có dấu đặc biệt như : hoặc #
            vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang " + order.getId());
            vnp_Params.put("vnp_OrderType", "other");
            vnp_Params.put("vnp_Locale", "vn");
            vnp_Params.put("vnp_ReturnUrl", VnPayConfig.vnp_ReturnUrl);
            vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

            // 3. Thời gian giao dịch
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            String vnp_CreateDate = formatter.format(cld.getTime());
            vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

            cld.add(Calendar.MINUTE, 15);
            String vnp_ExpireDate = formatter.format(cld.getTime());
            vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

            // 4. Xây dựng HashData và QueryString
            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();
            List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
            Collections.sort(fieldNames);

            Iterator<String> itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = vnp_Params.get(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    // Encode chuẩn VNPAY (space = %20)
                    String encodedValue = URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString())
                            .replace("+", "%20");

                    // HashData: Key thô, Value encode
                    hashData.append(fieldName).append('=').append(encodedValue);

                    // QueryString: Key encode, Value encode
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()))
                            .append('=')
                            .append(encodedValue);

                    if (itr.hasNext()) {
                        query.append('&');
                        hashData.append('&');
                    }
                }
            }

            // 5. Tạo chữ ký và URL cuối cùng
            String vnp_SecureHash = VnPayConfig.hmacSHA512(VnPayConfig.vnp_HashSecret, hashData.toString());
            String finalUrl = VnPayConfig.vnp_PayUrl + "?" + query.toString() + "&vnp_SecureHash=" + vnp_SecureHash;

            return finalUrl;
        } catch (Exception e) {
            return null;
        }
    }
}