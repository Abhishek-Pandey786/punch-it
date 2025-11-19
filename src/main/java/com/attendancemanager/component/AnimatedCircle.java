package com.attendancemanager.component;

import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class AnimatedCircle extends StackPane {
    private final Circle backgroundCircle;
    private final Circle outlineCircle;
    private final Text valueText;

    public AnimatedCircle(double radius, String color) {
        // Set fixed size for the container
        setMinSize(radius * 2, radius * 2);
        setPrefSize(radius * 2, radius * 2);
        setMaxSize(radius * 2, radius * 2);
        setAlignment(Pos.CENTER);

        // Outline circle (slightly larger)
        outlineCircle = new Circle(radius + 2);
        outlineCircle.setFill(Color.TRANSPARENT);
        outlineCircle.setStroke(Color.web(color));
        outlineCircle.setStrokeWidth(3);
        outlineCircle.setOpacity(0.4);

        // Background circle with vibrant color and shadow
        backgroundCircle = new Circle(radius);
        backgroundCircle.setStyle(String.format("-fx-fill: %s;", color));

        // Add drop shadow for depth
        DropShadow shadow = new DropShadow();
        shadow.setRadius(12);
        shadow.setOffsetX(0);
        shadow.setOffsetY(3);
        shadow.setColor(Color.rgb(0, 0, 0, 0.2));
        backgroundCircle.setEffect(shadow);

        // Value text with better formatting
        valueText = new Text("0");
        valueText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        valueText.setStyle("-fx-fill: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 2, 0, 0, 1);");

        getChildren().addAll(outlineCircle, backgroundCircle, valueText);
    }

    public void setValue(String text) {
        valueText.setText(text);
        // Adjust font size for percentage to fit better
        if (text.contains("%")) {
            valueText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        } else if (text.length() > 2) {
            valueText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        } else {
            valueText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        }
    }

    public void setColor(String color) {
        backgroundCircle.setStyle(String.format("-fx-fill: %s;", color));
        outlineCircle.setStroke(Color.web(color));
    }
}
