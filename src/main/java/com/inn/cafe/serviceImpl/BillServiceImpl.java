package com.inn.cafe.serviceImpl;

import com.inn.cafe.JWT.JwtFilter;
import com.inn.cafe.POJO.Bill;
import com.inn.cafe.constents.CafeConstants;
import com.inn.cafe.dao.BillDao;
import com.inn.cafe.service.BillService;
import com.inn.cafe.utils.CafeUtils;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.io.IOUtils;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@Service
public class BillServiceImpl implements BillService {


    @Autowired
    JwtFilter jwtFilter;

    @Autowired
    BillDao billDao;

    @Override
    public ResponseEntity<String> generateReport(Map<String, Object> requestMap) {
        try {
            String fileName;
            if(validateRequestMap(requestMap)) {
                if (requestMap.containsKey("isGenerate") && !(boolean) requestMap.get("isGenerate")) {
                    fileName = (String) requestMap.get("uuid");
                } else {
                    fileName = CafeUtils.getUuid();
                    requestMap.put("uuid",fileName);
                    insertBill(requestMap);
                }

                String data = "Name: " + requestMap.get("name") +"\n" + "Contact Number: " + requestMap.get("contactNumber")+ "\n" +
                        "Email: " + requestMap.get("email") + "\n" + "Payment Method: " + requestMap.get("paymentMethod") +"\n"+
                        "Address: "+requestMap.get("cafeAddress");

                Document document = new Document();

                File directory = new File(CafeConstants.STORE_LOCATION);
                if (!directory.exists()) {
                    boolean created = directory.mkdirs();
                    System.out.println("Directory created: " + created);
                }

                PdfWriter.getInstance(document, new FileOutputStream(CafeConstants.STORE_LOCATION+"\\"+fileName+".pdf"));
                document.open();
                setRectangleInPDF(document) ;

                Paragraph chunk = new Paragraph("\n \n Cafe Management System", getFont("Header"));
                chunk.setAlignment(Element.ALIGN_CENTER);
                document.add(chunk);


                Paragraph paragraph = new Paragraph(data +"\n \n",getFont("Data"));
                document.add(paragraph);

                PdfPTable table = new PdfPTable(5);
                table.setWidthPercentage(100);
                addTableHeader(table);

                JSONArray jsonArray = CafeUtils.getJsonArrayFromString((String) requestMap.get("productDetails"));
                for(int i=0;i<jsonArray.length();i++) {
                    addRow(table, CafeUtils.getMapFromJson(jsonArray.getString(i)));
                }
                document.add(table);

                Paragraph footer = new Paragraph("Total: "+requestMap.get("totalAmount")+"\n"
                +"Thank you for visiting.Please visit again!",getFont("Data"));

                document.add(footer);

                document.close();
                return new ResponseEntity<>("{\"uuid\":\""+fileName+"\"}",HttpStatus.OK);

            }
            return CafeUtils.getResponseEntity("Required data not found", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    private void addRow(PdfPTable table, Map<String, Object> data) {
        log.info("Inside addRow" );
        table.addCell((String) data.get("name"));
        table.addCell((String) data.get("category"));
        table.addCell((String) data.get("quantity"));
        table.addCell(Double.toString((Double)data.get("price")));
        table.addCell(Double.toString((Double) data.get("total")));
    }

    private void addTableHeader(PdfPTable table) {
        log.info("Log inside addTableHeader :");
        Stream.of("Name", "Category","Quantity","Price","Sub Total")
                .forEach(columnTitle->{
                    PdfPCell header = new PdfPCell();
                    header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    header.setBorderWidth(2);
                    header.setPhrase(new Phrase(columnTitle));
                    header.setBackgroundColor(BaseColor.YELLOW);
                    header.setHorizontalAlignment(Element.ALIGN_CENTER);
                    header.setVerticalAlignment(Element.ALIGN_CENTER);
                    table.addCell(header);

                });

    }

    private Font getFont(String type) {
        log.info("Inside get font");
        switch(type) {
            case "Header":
                Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLDOBLIQUE,18,BaseColor.BLACK);
                //headerFont.setSize(Font.BOLD);
                return headerFont;

            case "Data":
                Font dataFont = FontFactory.getFont(FontFactory.TIMES_ROMAN,18,BaseColor.BLACK);
                dataFont.setStyle(Font.BOLD);
                return dataFont;

            default:
                return new Font();

        }
    }

    private void setRectangleInPDF(Document document) throws DocumentException {
        log.info("Inside set rectangular in pdf");
        Rectangle react = new Rectangle(577,825,18,15);
        react.enableBorderSide(1);
        react.enableBorderSide(2);
        react.enableBorderSide(3);
        react.enableBorderSide(4);
        react.enableBorderSide(8);
        react.setBorderColor(BaseColor.BLACK);
        react.setBorderWidth(1);
        document.add(react);
    }

    private void insertBill(Map<String, Object> requestMap) {
        try {
            Bill bill = new Bill();
            bill.setUuid((String)requestMap.get("uuid"));
            bill.setName((String) requestMap.get("name"));
            bill.setEmail((String) requestMap.get("email"));
            bill.setContactNumber((String) requestMap.get("contactNumber"));
            bill.setTotalAmount(Integer.parseInt(((String) requestMap.get("totalAmount"))));
            bill.setPaymentMethod((String) requestMap.get("paymentMethod"));
            bill.setProductDetails((String)requestMap.get("productDetails"));
            bill.setCafeAddress((String) requestMap.get("cafeAddress"));
            bill.setCreatedBy(jwtFilter.getCurrentUser());
            billDao.save(bill);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean validateRequestMap(Map<String, Object> requestMap) {
        log.info("inside Generate Report");
        return requestMap.containsKey("name") &&
                requestMap.containsKey("contactNumber") &&
                requestMap.containsKey("paymentMethod") &&
                requestMap.containsKey("productDetails") &&
                requestMap.containsKey("totalAmount");
    }


    @Override
    public ResponseEntity<List<Bill>> getBills() {
            List<Bill> list = new ArrayList<>();
            if(jwtFilter.isAdmin()) {
                list = billDao.getAllBills();

            } else {
                list = billDao.getBillByCreatedBy(jwtFilter.getCurrentUser());
            }
        return new ResponseEntity<>(list,HttpStatus.OK);
    }

    @Override
    public ResponseEntity<byte[]> getPdf(Map<String, Object> requestMap) {
        log.info("Inside GetPDF : requestMap {} ",requestMap);
        try {
            byte[] byteArray = new byte[0];
            if(!requestMap.containsKey("uuid") && validateRequestMap(requestMap)) {
                return new ResponseEntity<>(byteArray,HttpStatus.BAD_REQUEST);
            }
            String filePath = CafeConstants.STORE_LOCATION+"\\" +requestMap.get("uuid") + ".pdf";
            if(CafeUtils.isFileExist(filePath)) {
                byteArray = getByteArray(filePath);
                return new ResponseEntity<>(byteArray,HttpStatus.OK);
            } else {
                requestMap.put("isGenerate",false);
                generateReport(requestMap);
                byteArray = getByteArray(filePath);
                return  new ResponseEntity<>(byteArray,HttpStatus.OK);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



    private byte[] getByteArray(String filePath) throws Exception{
        File initailFile = new File(filePath);
        InputStream targetStram = new FileInputStream(initailFile);
        byte[] byteArray = IOUtils.toByteArray(targetStram);
        targetStram.close();
        return byteArray;
    }

    @Override
    public ResponseEntity<String> deleteBill(Integer id) {
        try {
            Optional optional = billDao.findById(id);
            if(!optional.isEmpty()) {
                billDao.deleteById(id);
                return CafeUtils.getResponseEntity("Bill Deleted Successfully",HttpStatus.OK);
            }
            return CafeUtils.getResponseEntity("Bill Does not exist",HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG,HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
