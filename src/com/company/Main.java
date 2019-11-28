package com.company;

import java.io.*;
import java.math.BigInteger;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main {


    public static void main(String[] args) throws IOException, ParseException {

        ArrayList<StorageItem> warehouseItems = new ArrayList();
        ArrayList<StorageItem> unsortedList = new ArrayList();
        String row = "";

        if (warehouseDataUpdated()) {
            System.out.println("There hasnt been any known warehouse sheet updates since last progam run");
            warehouseItems = loadWarehouseItems();
        } else {
            System.out.println("warehouse sheet was updated since last run, so progam will update");
            unsortedList = createCurrentWarehouseItemsList();
            warehouseItems = sortAndFilterFromDuplicates(unsortedList, getLastWarehouseDataUpdateDate());
            System.out.println("Progam finished updating");
            System.out.println();
        }

        menu();


        boolean continueProgram = true;
        do {
            int switchCase = switchCaseValue();
            switch (switchCase) {
                case 0:
                    menu();
                    break;
                case 1:
                    System.out.println(warehouseItems);
                    System.out.println("To see menu press '0'");
                    break;
                case 2:
                    getListOfItemsBelowGivenQuantity(warehouseItems);

                    break;
                case 3:
                    getListOfItemsNearGivenExpirationDate(warehouseItems);
                    break;
                case 4:
                    unsortedList = createCurrentWarehouseItemsList();
                    warehouseItems = sortAndFilterFromDuplicates(unsortedList, getLastWarehouseDataUpdateDate());
                    System.out.println("Progam finished updating");
                    break;
                case 5:
                    System.out.println("Progam will terminate");
                    continueProgram = false;
                    break;

            }
        } while (continueProgram);


    }

    public static boolean warehouseDataUpdated() throws IOException {
        Long lastUpdated = getLastWarehouseDataUpdateDate();
        String lastKnownUpdate = getLastKnownUpdateDate();

        if (lastKnownUpdate.equals(lastUpdated.toString())) {
            return true;
        } else {
            return false;
        }
    }

    public static long getLastWarehouseDataUpdateDate() {

        try {
            File warehouseDataFile = new File("src/sample.csv");
            return warehouseDataFile.lastModified();
        }catch (Exception e){
            System.out.println("Warehouse sheet is not found. Terminating program");
            System.exit(0);
        }
        return 74960845690l;
    }

    public static String getLastKnownUpdateDate() throws IOException {
        try {
            BufferedReader br2 = new BufferedReader(new FileReader("src/date.txt"));
            String lastKnownUpdate = br2.readLine();
            br2.close();
        }catch (Exception e){
            saveNewUpdateDate(getLastWarehouseDataUpdateDate());
        }
        return ""+getLastWarehouseDataUpdateDate();
    }

    public static void saveNewUpdateDate(Long lastUpdated) throws IOException {
            BufferedWriter writer = new BufferedWriter(new FileWriter("src/date.txt"));
            writer.write("" + lastUpdated);
            writer.close();

    }

    public static ArrayList<StorageItem> loadWarehouseItems() throws IOException {
        ArrayList<StorageItem> warehouseItems = new ArrayList<>();
        try {
            FileInputStream fi = new FileInputStream(new File("src/warehouseListOfItemObjects.txt"));
            ObjectInputStream oi = new ObjectInputStream(fi);
            warehouseItems = (ArrayList<StorageItem>) oi.readObject();
            oi.close();
            fi.close();

        } catch (FileNotFoundException e) {
            System.out.println("File not found. Loading up data from Warehouse sheet");
            System.out.println();
            warehouseItems = sortAndFilterFromDuplicates(createCurrentWarehouseItemsList(), getLastWarehouseDataUpdateDate());
            System.out.println("Upload complete");
        } catch (IOException e) {
            System.out.println("Error initializing stream");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return warehouseItems;
    }

    public static ArrayList<StorageItem> createCurrentWarehouseItemsList() {
        ArrayList<StorageItem> unsortedList = new ArrayList();
        String row = "";
        try {
            FileReader fr = new FileReader("src/sample.csv");
            BufferedReader br = new BufferedReader(fr);
            br.readLine();
            while ((row = br.readLine()) != null) {
                String[] itemParam = row.split(",");
                String name = "-1";
                BigInteger code = new BigInteger(String.valueOf(-1));
                int quantity = -1;
                LocalDate expDate = LocalDate.parse("0001-01-01");
                if (!(itemParam[0]).equals("")) {
                    name = itemParam[0];
                }
                if (!(itemParam[1]).equals("")) {
                    code = new BigInteger((itemParam[1]));
                }
                if (!(itemParam[2]).equals("")) {
                    quantity = Integer.valueOf(itemParam[2]);
                }
                if (!(itemParam[3]).equals("")) {
                    String[] time = itemParam[3].split("-");
                    if (time[1].length() == 1) {
                        time[1] = "0" + time[1];
                    }
                    if (time[2].length() == 1) {
                        time[2] = "0" + time[2];
                    }
                    itemParam[3] = time[0] + "-" + time[1] + "-" + time[2];
                    expDate = LocalDate.parse(time[0] + "-" + time[1] + "-" + time[2]);
                }
                StorageItem item = new StorageItem(name, code, quantity, expDate);
                unsortedList.add(item);
            }
            fr.close();
            br.close();
        } catch (Exception e) {
            System.out.println("Bad file or path to it");
            System.exit(0);
        }
        return unsortedList;

    }

    public static ArrayList<StorageItem> sortAndFilterFromDuplicates(ArrayList<StorageItem> unsortedList, long lastUpdated) throws IOException {
        ArrayList<StorageItem> warehouseItems = new ArrayList();
        warehouseItems = (ArrayList<StorageItem>) unsortedList.stream()
                .sorted(Comparator.comparing(StorageItem::getCode))
                .collect(Collectors.toList());
        for (int i = 1; i < warehouseItems.size(); i++) {
            if (warehouseItems.get(i - 1).code.equals(warehouseItems.get(i).code)) {
                if (warehouseItems.get(i - 1).name.equals(warehouseItems.get(i).name)) {
                    if (warehouseItems.get(i - 1).expDate.compareTo(warehouseItems.get(i).expDate) == 0) {
                        warehouseItems.get(i - 1).quantity += warehouseItems.get(i).quantity;
                        warehouseItems.remove(i);
                        i--;
                    }
                }
            }
        }
        warehouseItems = (ArrayList<StorageItem>) unsortedList.stream()
                .sorted(Comparator.comparing(StorageItem::getName))
                .collect(Collectors.toList());
        FileOutputStream f = new FileOutputStream(new File("src/warehouseListOfItemObjects.txt"));
        ObjectOutputStream o = new ObjectOutputStream(f);
        o.writeObject(warehouseItems);
        o.close();
        f.close();
        saveNewUpdateDate(lastUpdated);
        return warehouseItems;
    }

    public static void menu() {
        System.out.println();
        System.out.println("To see whole list of items press '1'");
        System.out.println("To see what items are running low press '2'");
        System.out.println("To see whats near expiration date press '3'");
        System.out.println("To force program update to newest data from warehouseSheet press '4'");
        System.out.println("To terminate program press '5'");
        System.out.println();
    }

    public static int switchCaseValue() {
        Scanner in = new Scanner(System.in);
        boolean correctInput = true;

        int intVal = 0;
        do {
            String userInput = in.nextLine();
            try {
                intVal = Integer.valueOf(userInput);
                if (intVal == 0 | intVal == 1 | intVal == 2 | intVal == 3 | intVal == 4 | intVal == 5) {
                    correctInput = false;
                    return intVal;
                }
            } catch (Exception e) {
                System.out.println("Please enter correct number. To see menu press '0'");
            }
        } while (correctInput);
        return intVal;
    }

    public static void getListOfItemsBelowGivenQuantity(ArrayList<StorageItem> warehouseItems) {
        boolean rewindCase;
        do {
            System.out.println("Enter quantity:");
            Scanner in = new Scanner(System.in);
            String userInput = in.nextLine();
            int quantity = 0;
            try {
                quantity = Integer.valueOf(userInput);
                rewindCase = false;
            } catch (Exception e) {
                System.out.println("Please enter valid number");
                rewindCase = true;
            }
            ArrayList<StorageItem> list = new ArrayList<>();
            for (int i = 0; i < warehouseItems.size(); i++) {
                if (warehouseItems.get(i).quantity < quantity) {
                    list.add(warehouseItems.get(i));
                }
            }
            System.out.println(list);
            System.out.println("To see menu press '0'");
        } while (rewindCase);
    }

    public static void getListOfItemsNearGivenExpirationDate(ArrayList<StorageItem> warehouseItems) {
        boolean rewindCase;
        System.out.println("Enter date in format yyyy-MM-dd, (2012-12-31):");
        do {
            Scanner in = new Scanner(System.in);
            String userInput = in.nextLine();
            int quantity = 0;
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate localDate = LocalDate.parse(userInput, formatter);
                rewindCase = false;
                ArrayList<StorageItem> list = new ArrayList<>();
                for (int i = 0; i < warehouseItems.size(); i++) {
                    if (warehouseItems.get(i).getExpDate().isBefore(localDate)) {
                        list.add(warehouseItems.get(i));
                    }
                }
                System.out.println(list);
                System.out.println("To see menu press '0'");
            } catch (Exception e) {
                System.out.println("Please enter valid date in format yyyy/MM/dd, (2012/12/31).");
                rewindCase = true;

            }
        } while (rewindCase);
    }
}


