package service;

import java.io.*;
import java.net.*;
import java.util.Base64;
import org.json.*;

public class MidtransQrisService {

    private static final String SERVER_KEY = System.getenv("MIDTRANS_SERVER_KEY");

    private static final String CHARGE_URL = "https://api.sandbox.midtrans.com/v2/charge";
    private static final String STATUS_URL_PREFIX = "https://api.sandbox.midtrans.com/v2/";

    public static class QrisResult {
        public final String qrString;      // EMV string, untuk generate gambar QR
        public final String qrImageUrl;    // URL gambar QR, untuk di-paste ke simulator
        public final String transactionId;
        public final String orderId;

        public QrisResult(String qrString, String qrImageUrl, String transactionId, String orderId) {
            this.qrString = qrString;
            this.qrImageUrl = qrImageUrl;
            this.transactionId = transactionId;
            this.orderId = orderId;
        }
    }

    public static QrisResult createQrisTransaction(String orderId, long amount) throws Exception {
        if (SERVER_KEY == null || SERVER_KEY.isEmpty()) {
            throw new IllegalStateException("Server Key Midtrans tidak ditemukan!");
        }

        String auth = Base64.getEncoder().encodeToString((SERVER_KEY + ":").getBytes("UTF-8"));

        JSONObject body = new JSONObject()
            .put("payment_type", "qris")
            .put("transaction_details", new JSONObject()
                .put("order_id", orderId)
                .put("gross_amount", amount))
            .put("qris", new JSONObject()
                .put("acquirer", "gopay"));

        URL url = new URL(CHARGE_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Basic " + auth);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(15000);
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.toString().getBytes("UTF-8"));
        }

        int responseCode = conn.getResponseCode();

        InputStream is = (responseCode >= 200 && responseCode < 300)
            ? conn.getInputStream()
            : conn.getErrorStream();

        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
        }

        System.out.println("[QRIS] Response code: " + responseCode);
        System.out.println("[QRIS] Response body: " + sb);

        if (responseCode == 401) {
            throw new Exception("Autentikasi gagal (401). Periksa Server Key Midtrans kamu.\nResponse: " + sb);
        } else if (responseCode >= 400) {
            throw new Exception("Request gagal (" + responseCode + ").\nResponse: " + sb);
        }

        JSONObject json = new JSONObject(sb.toString());

        // ── Ambil qr_string (untuk generate gambar QR lokal) ──
        String qrString = json.optString("qr_string", "").trim();

        // ── Ambil qr_image_url (untuk di-paste ke simulator Midtrans) ──
        String qrImageUrl = "";
        if (json.has("actions")) {
            JSONArray actions = json.getJSONArray("actions");
            for (int i = 0; i < actions.length(); i++) {
                JSONObject act = actions.getJSONObject(i);
                if ("generate-qr-code".equals(act.optString("name"))) {
                    qrImageUrl = act.optString("url", "").trim();
                    break;
                }
            }
        }

        System.out.println("[QRIS] qr_string (panjang=" + qrString.length() + "): " + qrString);
        System.out.println("[QRIS] qr_image_url: " + qrImageUrl);

        if (qrString.isEmpty() && qrImageUrl.isEmpty()) {
            throw new Exception("QR string maupun QR image URL tidak ditemukan di response.\nResponse: " + json);
        }

        return new QrisResult(
            qrString,
            qrImageUrl,
            json.optString("transaction_id", orderId),
            orderId
        );
    }

    public static String checkTransactionStatus(String orderId) throws Exception {
        if (SERVER_KEY == null || SERVER_KEY.isEmpty()) {
            throw new IllegalStateException("Server Key Midtrans tidak ditemukan!");
        }

        String auth = Base64.getEncoder().encodeToString((SERVER_KEY + ":").getBytes("UTF-8"));
        String statusUrl = STATUS_URL_PREFIX + orderId + "/status";

        URL url = new URL(statusUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Basic " + auth);
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(15000);

        int responseCode = conn.getResponseCode();

        InputStream is = (responseCode >= 200 && responseCode < 300)
            ? conn.getInputStream()
            : conn.getErrorStream();

        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
        }

        if (responseCode >= 400) {
            throw new Exception("Gagal cek status (" + responseCode + ").\nResponse: " + sb);
        }

        JSONObject json = new JSONObject(sb.toString());
        return json.optString("transaction_status", "unknown");
    }
}