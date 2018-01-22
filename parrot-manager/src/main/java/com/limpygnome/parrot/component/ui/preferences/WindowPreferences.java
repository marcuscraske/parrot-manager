package com.limpygnome.parrot.component.ui.preferences;

import com.limpygnome.parrot.component.ui.WebViewStage;

public class WindowPreferences
{
    private double width;
    private double height;
    private double x;
    private double y;
    private boolean maximized;

    public WindowPreferences()
    {
        // Default window settings
        width = 1200.0;
        height = 700.0;
        maximized = true;
    }

    public void apply(WebViewStage stage)
    {
        stage.setWidth(width);
        stage.setHeight(height);

        if (maximized)
        {
            stage.setMaximized(true);
        }
        else if (x != 0.0 && y != 0.0)
        {
            stage.setX(x);
            stage.setY(y);
        }
    }


    public boolean copyFrom(WebViewStage stage)
    {
        double width = stage.getWidth();
        double height = stage.getHeight();
        double x = stage.getX();
        double y = stage.getY();
        boolean maximized = stage.isMaximized();

        boolean changed = this.width != width
                || this.height != height
                || this.x != x
                || this.y != y
                || this.maximized != maximized;

        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;
        this.maximized = maximized;

        return changed;
    }

    public double getWidth()
    {
        return width;
    }

    public void setWidth(double width)
    {
        this.width = width;
    }

    public double getHeight()
    {
        return height;
    }

    public void setHeight(double height)
    {
        this.height = height;
    }

    public double getX()
    {
        return x;
    }

    public void setX(double x)
    {
        this.x = x;
    }

    public double getY()
    {
        return y;
    }

    public void setY(double y)
    {
        this.y = y;
    }

    public boolean isMaximized()
    {
        return maximized;
    }

    public void setMaximized(boolean maximized)
    {
        this.maximized = maximized;
    }

}
