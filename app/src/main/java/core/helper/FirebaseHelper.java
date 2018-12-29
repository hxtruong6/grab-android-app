package core.helper;

import android.location.Location;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import core.Booking;

public class FirebaseHelper {

    public static boolean hasUser() {
        return FirebaseAuth.getInstance().getCurrentUser()!=null;
    }

    public static FirebaseUser getUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public static void sendBookingLocation(Booking booking) {
       //Trường DO cái này
       /*
       - Viết vào customerRequsest
        - Hàm này là customer bắt đầu gủi request lên server, t có truyền vào cái Booking (startLocation với end location)
        - Ghi requset lên server
        - lấy Uid nhanh: String strID = getUid();

       */
        
    }

    public static Driver receiveBookingResultFromFirebase(){
        /*Trường
        - Tìm 1 driver gần nhất
        - 
        - Hàm này trả về Driver đã nhận chuyenr mới gửi lên server
        - Trong này m đặt listener database, tạo query gì gì như bên SimCode làm
        - Chuyển availabe driver -> working driver
        - Xóa customerRequest
        */


    }

    public static Location getUpdateDriverLocation(String driverId){
        /*
        * Đặt reference tới workingDriver/driverUID/currentlocation
        */

        // reference.onChanged(){
        //     Customer.getInstance().getUpdateDriverLocation(loc);
        // }

        return MyHelper.createLocation(lat, lng);
    }

    private static String getUid() {
        return FirebaseHelper.getUser().getUid();
    }
}
