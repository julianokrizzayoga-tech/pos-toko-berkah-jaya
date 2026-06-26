import com.github.sarxos.webcam.Webcam;

public class TestKamera {
    public static void main(String[] args) {
        System.out.println("=== TEST KAMERA MULAI ===");
        try {
            System.out.println("Step 1: Mencari daftar kamera...");
            java.util.List<Webcam> list = Webcam.getWebcams();
            System.out.println("Step 2: Jumlah kamera ditemukan: " + list.size());
            for (Webcam w : list) {
                System.out.println("  - " + w.getName());
            }

            System.out.println("Step 3: Ambil kamera default...");
            Webcam webcam = Webcam.getDefault();
            if (webcam == null) {
                System.out.println("ERROR: Kamera tidak ditemukan!");
                return;
            }
            System.out.println("Step 4: Kamera ditemukan: " + webcam.getName());

            System.out.println("Step 5: Membuka kamera...");
            webcam.open();
            System.out.println("Step 6: Kamera berhasil dibuka!");

            Thread.sleep(1000);
            webcam.close();
            System.out.println("Step 7: Kamera ditutup. SEMUA OK!");

        } catch (Throwable t) {
            System.out.println("ERROR di step ini: " + t.getClass().getName());
            System.out.println("Pesan: " + t.getMessage());
            t.printStackTrace();
        }
    }
}