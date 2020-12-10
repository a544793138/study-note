package com.mastercard.cme.caas.api.core.jwe;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.util.encoders.Hex;
import org.junit.Test;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class TestPublicKey {



    @Test
    public void test_pk() throws IOException {
        final ASN1Sequence sequence = (ASN1Sequence) DERSequence.fromByteArray(Base64.getDecoder().decode("MIIBCgKCAQEAv3LGUwcfQ2GB2G26FPhPyHr8sznOYY53uaq3LOjWOtlua6CByfRoP5IyI3ZmlKYudJkf36TM5LKXRXAtuciI+V5EdBvFan5HNqXgVEU1x6PdzB9LNsIJIAG28Xs3Aaqkolbm5icdZE/3mNeihrKKTBWC4dHlZrBuhcVWmiarNdnkGVZIUsi73uiI/r8KqfU5uOLuUmdoheT3JuGnVOwyHysYJ5yA2Dql3DhJR2Wkd3scWdH78RKcYaT1WiFob8Cwwmox98BriDAIAJ0YUstguiMlXUYm+8rB9v2x/Xyird3154qhkzhgs1J47owY4fHQLoY0Rx+/H+PKACg9F39s8QIDAQAB")).toASN1Primitive();
        final ASN1Encodable modulus = sequence.getObjectAt(0);
        System.out.println(Hex.toHexString(((ASN1Integer)modulus.toASN1Primitive()).getValue().toByteArray()));
        System.out.println(Base64.getEncoder().encodeToString(((ASN1Integer)modulus.toASN1Primitive()).getValue().toByteArray()));
        final ASN1Encodable expoent = sequence.getObjectAt(1);
        System.out.println(Hex.toHexString(((ASN1Integer)expoent.toASN1Primitive()).getValue().toByteArray()));
    }

    @Test
    public void test_pk_with_oid() throws Exception {
        final String str = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAk2VIjX4Cc674levf9kXL5L3e4hqqVPqk1+fsCZpdfc0EqDrsQTTWLPc8OwIZRgRmFs8ydW4lvPNE7FiECSupFzMuw8VQEcsoy2uVK8eh6YFEI1oqcGpefWho23iTivkk61eJcmXvTMXTO/qYdIjGLSWnzTks+QWihpr/16yYSUD9UJ11gXaVmndY19XO+6QItcgTbhrOEf8fFtYKi0MI2lE9oag4WpeIq0VpxiWPs3xBHjbsZ+8k6zn2j89GNffz1SuYaHknuRhXHnQEzAh4XBfhTuv6e+ro+15pSt0e7jQGLn4dl+eKrU1XwS7aCXVRsNr1VPwMHq8JqoE2g1fvRQIDAQAB";

            final byte[] decode = Base64.getDecoder().decode(str);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decode);
            final RSAPublicKey rsaPublicKey = (RSAPublicKey) keyFactory.generatePublic(keySpec);
            final String s = Base64.getUrlEncoder().withoutPadding().encodeToString(rsaPublicKey.getModulus().toByteArray());
            System.out.println(s);
    }

}
