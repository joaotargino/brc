package tpanalytics.busroutecreator;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;

public class UploadFileToDropbox extends AsyncTask<Void, Void, Boolean> {

    private DropboxAPI<?> dropbox;
    private String path;
    private Context context;

    public UploadFileToDropbox(Context context, DropboxAPI<?> dropbox,
                               String path) {
        this.context = context.getApplicationContext();
        this.dropbox = dropbox;
        this.path = path;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        final File tempDir = context.getCacheDir();
        File tempFile;
        FileWriter fr;
        try {
//            tempFile = File.createTempFile("file", ".txt", tempDir);
//            fr = new FileWriter(tempFile);
//            fr.write("Sample text file created for demo purpose. You may use some other file format for your app ");
//            fr.close();
//
//            FileInputStream fileInputStream = new FileInputStream(tempFile);
//            dropbox.putFile(path + "textfile.txt", fileInputStream,
//                    tempFile.length(), null, null);
//            tempFile.delete();


            String filespath = context.getApplicationContext().getFilesDir().getPath();
//            System.out.println("Path: " + path);
            File f = new File(filespath);
            File file[] = f.listFiles();
//            System.out.println("Size: "+ file.length);
            for (int i=0; i < file.length; i++)
            {
                FileInputStream fileInputStream1 = new FileInputStream(file[i]);

                dropbox.putFile(path + file[i].getName(), fileInputStream1,
                        file[i].length(), null, null);
//                System.out.println( "FileName:" + file[i].getName());
            }


            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DropboxException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
//            try{
//                String filespath = context.getApplicationContext().getFilesDir().getPath();
//
//                File f = new File(filespath);
//                File file[] = f.listFiles();
//            System.out.println("Size: "+ file.length);
//                for (int i=0; i < file.length; i++) {
//                    boolean deleted = file[i].delete();
//                    System.out.println(deleted);
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
            Toast.makeText(context, "File Uploaded Sucesfully!",
                    Toast.LENGTH_LONG).show();

        } else {
            Toast.makeText(context, "Failed to upload file", Toast.LENGTH_LONG)
                    .show();
        }
    }
}