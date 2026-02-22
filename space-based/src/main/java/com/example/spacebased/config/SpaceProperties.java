package com.example.spacebased.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.space")
public class SpaceProperties {

    private int priceWindowMinutes = 15;
    private int alertTtlSeconds = 300;
    private double spikeThresholdMultiplier = 2.0;

    public int getPriceWindowMinutes() {
        return priceWindowMinutes;
    }

    public void setPriceWindowMinutes(int priceWindowMinutes) {
        this.priceWindowMinutes = priceWindowMinutes;
    }

    public int getAlertTtlSeconds() {
        return alertTtlSeconds;
    }

    public void setAlertTtlSeconds(int alertTtlSeconds) {
        this.alertTtlSeconds = alertTtlSeconds;
    }

    public double getSpikeThresholdMultiplier() {
        return spikeThresholdMultiplier;
    }

    public void setSpikeThresholdMultiplier(double spikeThresholdMultiplier) {
        this.spikeThresholdMultiplier = spikeThresholdMultiplier;
    }
}
