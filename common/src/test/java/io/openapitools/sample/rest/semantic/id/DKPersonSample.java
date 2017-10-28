package io.openapitools.sample.rest.semantic.id;

import java.util.Optional;

/**
 * Simple sample class to show the workings of the human readable non sensitive ID
 */
public class DKPersonSample implements NonSensitiveSemanticID {


    String firstName;
    String middleName;
    String sirName;
    String cpr;
    Optional<String> sid;


    public DKPersonSample(String firstName, String middleName, String sirName, String cpr) {
        this.firstName = firstName;
        this.middleName = middleName;
        this.sirName = sirName;
        this.cpr = cpr; //a valid cpr is not enforced....is this sample
        this.sid = Optional.empty();
    }

    @Override
    public String getHumanReadableNonSensitiveID() {
        if (!sid.isPresent()) {
            this.sid = Optional.of(constructSID());
        }
        return sid.get();
    }

    @Override
    public String adjustHumanReadableNonSensitiveID(long sequence) {
        if (sid.isPresent()) {
            sid = Optional.of(sid.get() + SEPARATOR + Long.toString(sequence));
        } else {
            sid = Optional.of(constructSID() + SEPARATOR + Long.toString(sequence));
        }
        return sid.get();
    }

    private String constructSID() {
        char separator = SEPARATOR;
        this.sid = Optional.of(getSIDPart(firstName, separator) +
                getSIDPart(middleName, separator) +
                getSIDPart(sirName, separator) +
                cpr.substring(0, 4));
        return sid.get();
    }

    private String getSIDPart(String part, char separator) {
        int length = LENGTH;
        String preparedPart = part.trim().toLowerCase().replace(" ","").replace("å","aa").replace("ø", "oe").replace("æ","ae");
        if (preparedPart.length() > 0) {
            return preparedPart.substring(0, preparedPart.length() < length ? preparedPart.length() : length) + separator;
        }
        return "";
    }

}
