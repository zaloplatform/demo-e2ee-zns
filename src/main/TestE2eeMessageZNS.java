/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.DatatypeConverter;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import org.whispersystems.libsignal.IdentityKey;
import org.whispersystems.libsignal.IdentityKeyPair;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.SessionBuilder;
import org.whispersystems.libsignal.SessionCipher;
import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.UntrustedIdentityException;
import org.whispersystems.libsignal.ecc.Curve;
import org.whispersystems.libsignal.ecc.ECKeyPair;
import org.whispersystems.libsignal.ecc.ECPublicKey;
import org.whispersystems.libsignal.protocol.CiphertextMessage;
import org.whispersystems.libsignal.state.PreKeyBundle;
import org.whispersystems.libsignal.state.SignalProtocolStore;
import org.whispersystems.libsignal.state.impl.InMemorySignalProtocolStore;
import org.whispersystems.libsignal.util.KeyHelper;
import service.HttpRequestService;

/**
 *
 * @author hienhh
 */
public class TestE2eeMessageZNS {

    private static final String accessToken = "";

    public static String convertByteToString(byte[] pubKey) {
        String result = "";
        try {
            result = DatatypeConverter.printBase64Binary(pubKey);
        } catch (Exception ex) {
        }
        return result;
    }

    public static byte[] convertStringToByte(String pubKey) {
        byte[] result = null;
        try {
            result = DatatypeConverter.parseBase64Binary(pubKey);
        } catch (Exception ex) {
        }
        return result;
    }

    private static IdentityKeyPair generateIdentityKeyPair() throws InvalidKeyException {
        ECKeyPair identityKeyPairKeys = Curve.generateKeyPair();

        return new IdentityKeyPair(new IdentityKey(identityKeyPairKeys.getPublicKey()),
                identityKeyPairKeys.getPrivateKey());
    }

    private static int generateRegistrationId() {
        return KeyHelper.generateRegistrationId(false);
    }

    private static String buildFinalMessageStr(CiphertextMessage param1Encrypt) {
        String result = "";
        try {
            int type = param1Encrypt.getType();

            ByteArrayOutputStream finalOut = new ByteArrayOutputStream();
            finalOut.write(type);
            finalOut.write(param1Encrypt.serialize());
            byte[] finalMessageParam1Byte = finalOut.toByteArray();

            result = Base64.encodeBase64String(finalMessageParam1Byte);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            ex.printStackTrace();
        }
        
        return result;
    }

    public static void main(String[] args) throws InvalidKeyException, UntrustedIdentityException, IOException {
        String phone = "";

        String e2EEKey = HttpRequestService.getE2EEKey(accessToken, phone);

        JSONObject responseE2EEKey = new JSONObject();
        try {
            responseE2EEKey = new JSONObject(e2EEKey);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            ex.printStackTrace();
            System.exit(0);
        }
        
        if (responseE2EEKey.optLong("error") < 0){
            System.err.println("error code: " + responseE2EEKey.optLong("error"));
            System.err.println("error message: " + responseE2EEKey.optString("message", ""));
            System.exit(0);
        }

        if (responseE2EEKey.has("data") == false) {
            System.err.println("get e2ee key fail, field data not found");
            System.exit(0);
        }

        JSONObject data = responseE2EEKey.optJSONObject("data");

        if (data == null) {
            System.err.println("response data is null");
            System.exit(0);
        }

        int deviceId = data.optInt("device_id", 0);

        String pubKeyComplete = data.optString("public_key_complete", "");
        int pubKeyId = data.optInt("public_key_id", 0);

        String identityKeyComplete = data.optString("identity_key_complete", "");
        int identityId = data.optInt("identity_id", 0);

        String signature = data.optString("signature", "");

        SignalProtocolAddress userAddress = new SignalProtocolAddress(phone, deviceId);

        SignalProtocolStore oaProtocolStore = new InMemorySignalProtocolStore(generateIdentityKeyPair(), generateRegistrationId());

        byte[] pubKeyArray = convertStringToByte(pubKeyComplete);
        ECPublicKey publicKey = Curve.decodePoint(pubKeyArray, 0);

        byte[] identityKeyArray = convertStringToByte(identityKeyComplete);
        IdentityKey identityKey = new IdentityKey(identityKeyArray, 0);

        PreKeyBundle userPreKeyBundle = new PreKeyBundle(identityId, deviceId,
                0, null,
                pubKeyId, publicKey,
                convertStringToByte(signature), identityKey);

        SessionBuilder oaSessionBuilder = new SessionBuilder(oaProtocolStore, userAddress);

        oaSessionBuilder.process(userPreKeyBundle);

        SessionCipher oaSessionCipher = new SessionCipher(oaProtocolStore, userAddress);

        CiphertextMessage param1Encrypt = oaSessionCipher.encrypt("message e2e from java sdk 31/05 - param 1".getBytes());
        String finalMessageParam1Str = buildFinalMessageStr(param1Encrypt);
        
        CiphertextMessage param2Encrypt = oaSessionCipher.encrypt("message e2e from java sdk 31/05 - param 2".getBytes());
        String finalMessageParam2Str = buildFinalMessageStr(param2Encrypt);

        CiphertextMessage param3Encrypt = oaSessionCipher.encrypt("message e2e from java sdk 31/05 - param 3".getBytes());
        String finalMessageParam3Str = buildFinalMessageStr(param3Encrypt);

        CiphertextMessage param4Encrypt = oaSessionCipher.encrypt("message e2e from java sdk 31/05 - param 4".getBytes());
        String finalMessageParam4Str = buildFinalMessageStr(param4Encrypt);

        CiphertextMessage param5Encrypt = oaSessionCipher.encrypt("message e2e from java sdk 31/05 - param 5".getBytes());
        String finalMessageParam5Str = buildFinalMessageStr(param5Encrypt);

        Map<String, String> templateData = new HashMap<>();
        templateData.put("param_1", finalMessageParam1Str);
        templateData.put("param_2", finalMessageParam2Str);
        templateData.put("param_3", finalMessageParam3Str);
        templateData.put("param_4", finalMessageParam4Str);
        templateData.put("param_5", finalMessageParam5Str);
        
        JSONObject templateDataJson = new JSONObject();
        if (templateData.keySet().size() > 0){
            for (String key : templateData.keySet()){
                templateDataJson.put(key, templateData.get(key));
            }
        }
        
        JSONObject body = new JSONObject();
        body.put("phone", phone);
        body.put("template_id", "223750");
        body.put("template_data", templateDataJson);
        
        System.err.println(body.toString());

        System.exit(0);
    }

}
