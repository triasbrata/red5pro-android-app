package infrared5.com.red5proandroid.utilities;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.view.View;

import java.util.Vector;

/**
 * Created by davidHeimann on 11/8/16.
 */

public class ValidationUtillity {

    private static Vector<ObjectAnimator> animationSets;

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

        if(streamName.isEmpty())
            return false;
        for (char c:streamName.toCharArray()) {
            if(Character.isWhitespace(c))
                return false;
        }

        return true;
    }

    public static void FlashRed(View view){

        if(animationSets == null){
            animationSets = new Vector<ObjectAnimator>();
        }
        else{
            for (ObjectAnimator animation: animationSets) {
                if(animation.getTarget() == view){
                    animation.cancel();
                }
            }
        }

        view.setBackgroundColor(Color.argb(255, 225, 25, 0));

        ObjectAnimator bgAnimate = ObjectAnimator.ofObject(view,"backgroundColor", new ArgbEvaluator(),0xFFE11900,0xFFFFFFFF );
        bgAnimate.setStartDelay(500);
        bgAnimate.setDuration(250);
        bgAnimate.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                animationSets.remove(this);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                animationSets.remove(this);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        bgAnimate.start();
    }

    public static void CancelFlashes(){
        if(animationSets != null && animationSets.size() > 0){
            for (ObjectAnimator animation: animationSets) {
                animation.cancel();
            }
        }
    }
}
