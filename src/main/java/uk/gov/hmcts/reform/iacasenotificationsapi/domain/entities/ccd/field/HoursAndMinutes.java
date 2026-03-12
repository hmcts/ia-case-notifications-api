package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field;

public class HoursAndMinutes {

    private  String hours;
    private  String minutes;

    public HoursAndMinutes(){

        // noop -- for deserializer
    }

    public HoursAndMinutes(String hours, String minutes) {
        this.hours = hours;
        this.minutes = minutes;

    }
    public String getHours(){
        return hours;
    }

    public String getMinutes(){
        return minutes;
    }
}




