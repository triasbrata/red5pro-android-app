package infrared5.com.red5proandroid.utilities;

/**
 * Created by davidHeimann on 11/8/16.
 */

public class ValidationUtillity {

    public static String ValidateIP(String ip){

        String[] ipSplit = ip.split("[.]");
        String errorString = "Server IP not in valid format - x.x.x.x";

        if(ipSplit.length == 4) {
            errorString = "";

            int i = 0;
            for (String val : ipSplit) {
                try {
                    int intVal = Integer.parseInt(val);

                    if(i == 0){
                        if( intVal < 0 || intVal > 255 ){
                            errorString = "The initial IP file must be between 1 and 255";
                            break;
                        }
                        i++;
                    }
                    else {
                        if( intVal <= 0 || intVal > 255 ){
                            errorString = "IP values must be between 0 and 255";
                            break;
                        }
                    }
                } catch (Exception e) {
                    errorString = "IP values must be numbers";
                    break;
                }
            }
        }

        return errorString;
    }

    public static boolean ValidatePort(String port){

        try{
            int portInt = Integer.parseInt(port);
            if(portInt > 0)
                return true;
        }catch (Exception e){}

        return false;
    }

    public static boolean ValidateName(String streamName){

        for (char c:streamName.toCharArray()) {
            if(Character.isWhitespace(c))
                return false;
        }

        return true;
    }
}
