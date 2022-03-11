package mail.controller.persistence;

import javafx.collections.FXCollections;
import javafx.scene.control.CheckBox;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PersistenceAccess {

    private String VALID_ACCOUNTS_LOCATION = System.getProperty("user.home") + File.separator + "validaccounts.ser";
    private boolean rememberMe;
//    private transient List<AccountRememberPreference> accountsRememberPreferences = new ArrayList<>();
    private boolean automaticLogin;
    private Encoder encoder = new Encoder();

    public List<ValidAccount> loadValidAccounts(){
        List<ValidAccount> resultList = new ArrayList<ValidAccount>();
        try(ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(VALID_ACCOUNTS_LOCATION))){
            List<ValidAccount> persistedList = (List<ValidAccount>) objectInputStream.readObject();
            decodePassword(persistedList);
            resultList.addAll(persistedList);
        }catch (IOException ioException){
            ioException.printStackTrace();
        } catch (ClassNotFoundException classNotFoundException){
            classNotFoundException.printStackTrace();
        }
        return resultList;
    }

    public void saveValidAccounts(List<ValidAccount> validAccounts){
        try(ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(VALID_ACCOUNTS_LOCATION))){
            encodePassword(validAccounts);
            objectOutputStream.writeObject(validAccounts);
        }catch (IOException ioException){
            ioException.printStackTrace();
        }
    }

    private void decodePassword(List<ValidAccount> validAccounts){
        for(ValidAccount validAccount : validAccounts){
            validAccount.setPassword(encoder.decode(validAccount.getPassword()));
        }
    }

    private void encodePassword(List<ValidAccount> validAccounts){
        for (ValidAccount validAccount : validAccounts){
            validAccount.setPassword(encoder.encode(validAccount.getPassword()));
        }
    }

    public void saveAccountsLoginPreference(CheckBox rememberCheckBox) {
//         Map<String,Boolean> accountsRememberPreferences = new HashMap<>();
//        try(ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream("rememberPreference.dat"))){
//            accountsRememberPreferences.put(address,rememberCheckBox.isSelected());
//            for(String key : accountsRememberPreferences.keySet()) {
//                System.out.println("On save " + key + " " +accountsRememberPreferences.get(key));
//            }
//            objectOutputStream.writeObject(accountsRememberPreferences);
//        }catch (IOException ioException){
//            ioException.printStackTrace();
//        }
        try(BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("accountsLoginPreference.dat"))){
            bufferedWriter.write(rememberCheckBox.isSelected() + "");
        }catch (IOException ioException){
            ioException.printStackTrace();
        }
    }

    public boolean readAccountPreference(){
//        try(ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream("rememberPreference.dat"))){
//            Map<String,Boolean> accountsRememberPreferences =  (Map<String,Boolean>) objectInputStream.readObject();
//            return accountsRememberPreferences;
//        }catch (ClassNotFoundException classNotFoundException){
//            classNotFoundException.printStackTrace();
//        } catch (IOException ioException){
//            ioException.printStackTrace();
//        }
//        return null;
        try(BufferedReader bufferedReader = new BufferedReader(new FileReader("accountsLoginPreference.dat"))){
            automaticLogin = Boolean.parseBoolean(bufferedReader.readLine());
            return automaticLogin;
        }catch (IOException ioException){
            ioException.printStackTrace();
        }
        return false;
    }
}
