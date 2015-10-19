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

        int yellow = Color.LTGRAY;
        int yellowR = Color.red(yellow);
        int yellowG = Color.green(yellow);
        int yellowB = Color.blue(yellow);

        int green = Color.GREEN;
        int greenR = Color.red(green);
        int greenG = Color.green(green);
        int greenB = Color.blue(green);

        double resultR;
        double resultG;
        double resultB;

        if(sent>=0){
            double percent = sent/2;
            resultR = yellowR + percent*(greenR-yellowR);
            resultG = yellowG + percent*(greenG-yellowG);
            resultB = yellowB + percent*(greenB-yellowB);

        }else{
            double percent = -1*sent/2;
            resultR = redR + percent*(yellowR-redR);
            resultG = redG + percent*(yellowG-redG);
            resultB = redB + percent*(yellowB-redB);
        }


        return Color.rgb((int) resultR, (int) resultG, (int)resultB);
    }
}