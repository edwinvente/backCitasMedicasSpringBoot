package co.com.rapicredit.batchpayments.hexagonal.application.service;

import co.com.rapicredit.batchpayments.hexagonal.domain.models.response.AwsResponse;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class AwsServiceAdapter {
    @Value("${amazonS3.accessKey}")
    private String accessKey;
    @Value("${amazonS3.secretKey}")
    private String secretKey;
    @Value("${amazonS3.bucketName}")
    private String bucketName;
    public AwsResponse uploadFileToS3(MultipartFile file) {
        AwsResponse response = new AwsResponse();
        String fileName = "";
        try {
            // Crea una instancia de AmazonS3 utilizando las credenciales de autenticación y la región predeterminada
            AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
            AmazonS3 s3client = AmazonS3ClientBuilder
                    .standard()
                    .withCredentials(new AWSStaticCredentialsProvider(credentials))
                    .withRegion("us-east-1")
                    .build();

            if (!this.isValidFile(file, response)) return response;
            // Crea una solicitud de subida de objeto con el bucket y el nombre del archivo en S3
            File myFile = convertMultiPartToFile(file);
            if (myFile == null) {
                response.setStatus(false);
                response.setMessage("Error de conversión en el archivo ");
                response.setPatch("Error de conversión en el archivo ");
                return response;
            }
            fileName = this.creteUniqueNameToFile(file);
            PutObjectRequest request = new PutObjectRequest(bucketName, fileName, myFile);
            // Establece la metadata del archivo
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            metadata.addUserMetadata("x-amz-meta-title", "batchPayments-service");
            request.setMetadata(metadata);
            // Sube el archivo a S3
            s3client.putObject(request);
        } catch (AmazonServiceException e) {
            response.setStatus(false);
            response.setMessage(e.getErrorMessage());
            response.setPatch(e.getErrorMessage());
            return response;
        }
        response.setMessage("Proceso iniciado correctamente, te avisaremos apenas cuando el proceso termine...");
        response.setPatch("https://" + bucketName + ".s3.amazonaws.com/" + fileName);
        return response;
    }
    public boolean isValidFile(MultipartFile file, AwsResponse response){
        //validamos que se mande como minimo un archivo
        if (file.isEmpty()) {
            response.setStatus(false);
            response.setMessage("No se subio ningun archivo");
            response.setPatch("No se subio ningun archivo");
            return false;
        }
        //validamos el tipo de archivo
        String mimeType = file.getContentType();
        if (!mimeType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")){
            response.setStatus(false);
            response.setMessage("El formato del archivo es invalido... ");
            response.setPatch("El formato del archivo es invalido... ");
            return false;
        }

        return response.getStatus();
    }

    public String creteUniqueNameToFile(MultipartFile file){
        // Obtener la fecha y hora actual
        LocalDateTime now = LocalDateTime.now();
        // Formatear la fecha y hora en un string con el formato "yyyy-MM-dd-HH-mm-ss"
        String formattedDateTime = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));
        // Crear el nombre del archivo utilizando el string formateado
        return formattedDateTime + "-" + file.getOriginalFilename();
    }
    public File getFile(String fileKey) throws IOException {
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        AmazonS3 s3client = AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion("us-east-1")
                .build();
        // Obtiene el objeto de S3
        S3Object s3Object = s3client.getObject(new GetObjectRequest(bucketName, fileKey));
        // Crea un archivo temporal para almacenar el contenido del objeto
        File file = File.createTempFile("temp", ".tmp");
        // Escribe el contenido del objeto en el archivo temporal
        try (InputStream inputStream = s3Object.getObjectContent();
             FileOutputStream outputStream = new FileOutputStream(file)) {
            int read;
            byte[] bytes = new byte[1024];
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        }
        // Devuelve el archivo temporal
        return file;
    }
    private File convertMultiPartToFile(MultipartFile file) {
        try {
            File convFile = new File(file.getOriginalFilename());
            FileOutputStream fos = new FileOutputStream(convFile);
            fos.write(file.getBytes());
            fos.close();
            return convFile;
        } catch (IOException e){
            e.getMessage();
        }
       return null;
    }

}
