package io.openapitools.sample.rest.semantic.id;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NonSensitiveSemanticIDTest {


    @Test
    public void testNormalPersonID() {
        DKPersonSample hansPedersen = new DKPersonSample("Hans","Peter","Pedersen","123100-1234");
        assertEquals("hans-peter-pedersen-1231", hansPedersen.getHumanReadableNonSensitiveID());
    }

    @Test
    public void testPersonIDNoMiddlename() {
        DKPersonSample hansPedersen = new DKPersonSample("Hans","","Pedersen","123100-1234");
        assertEquals("hans-pedersen-1231", hansPedersen.getHumanReadableNonSensitiveID());
    }

    @Test
    public void testPersonIDShortMiddlename() {
        DKPersonSample hansPedersen = new DKPersonSample("Hans","C","Pedersen","123100-1234");
        assertEquals("hans-c-pedersen-1231", hansPedersen.getHumanReadableNonSensitiveID());
    }

    @Test
    public void testPersonIDLongSirname() {
        DKPersonSample hansPedersen = new DKPersonSample("Hans","C","Pedersen-Jensen-Olsen","123100-1234");
        assertEquals("hans-c-pedersen-j-1231", hansPedersen.getHumanReadableNonSensitiveID());
    }

    @Test
    public void testPersonIDLongName() {
        DKPersonSample hansPedersen = new DKPersonSample("Hans Georg Henrik","Jannerik Clauserik ","Pedersen-Jensen-Olsen","123100-1234");
        assertEquals("hansgeorgh-jannerikcl-pedersen-j-1231", hansPedersen.getHumanReadableNonSensitiveID());
    }

    @Test
    public void testPersonIDSpecialName() {
        DKPersonSample soerenHaagear = new DKPersonSample("Søren","HøstBlomst","Hågemæger","123100-1234");
        assertEquals("soeren-hoestbloms-haagemaege-1231", soerenHaagear.getHumanReadableNonSensitiveID());
    }

    @Test
    public void testPersonIDNonUnique() {
        DKPersonSample soerenHaagear = new DKPersonSample("Søren","P","Æblegård","123100-1234");
        assertEquals("soeren-p-aeblegaard-1231", soerenHaagear.getHumanReadableNonSensitiveID());
        assertEquals("soeren-p-aeblegaard-1231-1", soerenHaagear.adjustHumanReadableNonSensitiveID(1));
        soerenHaagear = new DKPersonSample("Søren","P","Æblegård","123100-1234");
        assertEquals("soeren-p-aeblegaard-1231-2", soerenHaagear.adjustHumanReadableNonSensitiveID(2));
        soerenHaagear = new DKPersonSample("Søren","P","Æblegård","123100-1234");
        assertEquals("soeren-p-aeblegaard-1231-999", soerenHaagear.adjustHumanReadableNonSensitiveID(999));
    }

}

