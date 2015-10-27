package com.taylorandtucker.jot.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by tuckerkirven on 10/11/15.
 */
class GradientView extends View {
    public GradientView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public GradientView(Context context) {
        super(context);
    }
    public GradientView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setgradient(double sent){
        GradientDrawable d = new GradientDrawable(GradientDrawable.Orientation.BL_TR,
                new int[] {Color.WHITE,getColorFromGradient(sent)});
        d.setCornerRadius(0f);
        this.setBackground(d);
    }
    public static int getColorFromGradient(double sent){
    /*double resultRed = color1.red + percent * (color2.red - color1.red);
    double resultGreen = color1.green + percent * (color2.green - color1.green);
    double resultBlue = color1.blue + percent * (color2.blue - color1.blue);
    */

        int red = Color.RED;
        int redR = Color.red(red);
        int redG = Color.green(red);
        int redB = Color.blue(red);

        int gray = Color.LTGRAY;
        int grayR = Color.red(gray);
        int grayG = Color.green(gray);
        int grayB = Color.blue(gray);

        int green = Color.GREEN;
        int greenR = Color.red(green);
        int greenG = Color.green(green);
        int greenB = Color.blue(green);

        double resultR;
        double resultG;
        double resultB;

        if(sent>=0){
            System.out.println("sent > 0: " + sent);
            double percent = sent/2.0;
            resultR = grayR + percent*(greenR-grayR);
            resultG = grayG + percent*(greenG-grayG);
            resultB = grayB + percent*(greenB-grayB);

        }else{
            System.out.println("sent < 0 : " + sent);
            double percent = -1*sent/2.0;
            System.out.println("percent : " + percent);
            resultR = grayR + percent*(redR-grayR);
            resultG = grayG + percent*(redG-grayG);
            resultB = grayB + percent*(redB-grayB);

            System.out.println("RGB " + resultR + " " + resultG + " " + resultB + " ");
        }


        return Color.rgb((int) resultR, (int) resultG, (int)resultB);
    }
}