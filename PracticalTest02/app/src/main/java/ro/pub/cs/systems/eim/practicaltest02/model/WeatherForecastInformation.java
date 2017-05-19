package ro.pub.cs.systems.eim.practicaltest02.model;

public class WeatherForecastInformation {

    private String cheie;
    private String minut;

    public WeatherForecastInformation() {
        this.cheie = null;
        this.minut = null;
    }

    public WeatherForecastInformation(String name, String lat) {
        this.cheie = name;
        this.minut = lat;
    }

    public String getName() {
        return this.cheie;
    }

    public void setName(String name) {
        this.cheie = name;
    }

    public void setLat(String lat) {
        this.minut = lat;
    }

    public String getMinut() {
        return this.minut;
    }

    @Override
    public String toString() {
        return "WeatherForecastInformation{" +
                "name='" + cheie + '\'' +
                ", lat='" + minut + '\'' +
                '}';
    }

}
