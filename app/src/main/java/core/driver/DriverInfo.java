package core.driver;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class DriverInfo {
    public String name;
    public String email;
    public String vehicle;
    public int star;
    public String phone;

    public DriverInfo(){
        name = "empty";
        email ="empty";
        vehicle = "empty";
        star = 0;
        phone = "empty";
    }

    public Boolean isEmpty(){
        return name.equals("empty") || name==null||name.isEmpty();
    }
    public DriverInfo(String xName, String xEmail, String xVehicle, String xPhone, int xStar){
        name = xName;
        email = xEmail;
        vehicle = xVehicle;
        phone = xPhone;
        star = xStar;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("email", email);
        result.put("vehicle", vehicle);
        result.put("phone", phone);
        result.put("star", star);

        return result;
    }


}
