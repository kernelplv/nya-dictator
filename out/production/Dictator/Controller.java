package sample;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;

import javafx.event.EventHandler;

import java.io.*;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Pair;

import javax.xml.crypto.Data;
import javafx.event.ActionEvent;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;


public class Controller {

    @FXML private AnchorPane RootPane;
    @FXML private VBox WorkPlace;
    @FXML private RadioMenuItem MenuItem_checkWrite;
    @FXML private RadioMenuItem MenuItem_checkTranslate;
    @FXML private Button MenuItem_buttonRandom;
    @FXML private MenuItem MenuItem_TextSize;
    @FXML private MenuItem MenuItem_NewTry;
    @FXML private MenuItem MenuItem_About;
    @FXML private Slider MenuItem_Slider;
    @FXML private ProgressBar MenuProgressBar;
    @FXML private Label txtTarget;
    @FXML private Label txtHint;



    /* NOT FXML*/
    private ArrayList<String> DBnya;
    private ArrayList<String> DBword;
    private ArrayList<String> DBerr; //Contains "wrong_word_index:count_of_errors"
    private ToggleGroup Mode;
    private Properties property;
    private File DataFile;

    /* NOT FXML - - - Process*/
    private static Integer index = 0;
    private static boolean hint = false;
    private static String mode = "";
    private static boolean random = false;
    private static final Double DefaultFontSizeTarget = 96.0;
    private static final Double DefaultFontSizeHint = 22.0;
    private static final String DefaultFontName = "System";


    @FXML
    public void initialize()
    {
        Mode = new ToggleGroup();
        DBnya = new ArrayList<>();
        DBword = new ArrayList<>();
        DBerr = new ArrayList<>();

        MenuItem_checkWrite.setUserData("write");
        MenuItem_checkWrite.setToggleGroup(Mode);
        MenuItem_checkTranslate.setUserData("read");
        MenuItem_checkTranslate.setToggleGroup(Mode);

        MenuItem_checkWrite.setSelected(true);

        txtTarget.setMouseTransparent(true);
        txtHint.setMouseTransparent(true);

        // try load config

        property = new Properties();

        try
        {
            FileInputStream in = new FileInputStream("config.properties");
            property.load(in);
            in.close();
        }
        catch (IOException e)
        {
            System.err.println("Файл настроек не найден!");
        }

        DataFile = new File(property.getProperty("DataBase",""));
        if (DataFile.exists())
        {
            index = Integer.parseInt(property.getProperty("index"))-1 ;

            index = index > 0 ? index : 0;

            ReadToDB(DataFile);

            NextTask(false);
            txtHint.setText("нажмите левую кнопку мыши, чтобы продолжить");
            txtTarget.setMouseTransparent(false);
            txtHint.setMouseTransparent(false);

            mode = property.getProperty("mode","read");

            if (mode.compareTo("read")==0) Mode.selectToggle(MenuItem_checkTranslate);
            else Mode.selectToggle(MenuItem_checkWrite);

            MenuItem_NewTry.setDisable(false);


        }
        else
        {
            MenuItem_NewTry.setDisable(true);
            txtHint.setText("выберите базу слов для повторения!");
        }


    }
    @FXML
    void MenuExit()
    {
        RootPane.getScene().getWindow().hide();
    }
    @FXML
    public void exitApplication()
    {
        try
        {
            property.setProperty("index",index.toString());
            property.store(new FileOutputStream("config.properties"),null);
            System.err.println("Настройки сохранены.");
        }
        catch (Exception e)
        {
            System.err.println("Не удалось сохранить настройки.");
        }
        finally {
            Platform.exit();
        }
        System.out.println("Выход совершен.");
    }
    @FXML
    void MenuWriteCheck()
    {
       mode = Mode.getSelectedToggle().getUserData().toString();
       property.setProperty("mode",mode);
       System.out.println(Mode.getSelectedToggle().getUserData());
    }
    @FXML
    void MenuTranslateCheck()
    {
        mode = Mode.getSelectedToggle().getUserData().toString();
        property.setProperty("mode",mode);
        System.out.println(Mode.getSelectedToggle().getUserData());
    }
    @FXML
    void MenuRandomClick()
    {
        Integer newRnd = ThreadLocalRandom.current().nextInt();
        Collections.shuffle(DBnya,new Random(newRnd));
        Collections.shuffle(DBword,new Random(newRnd));
        NextTask(true);
    }
    @FXML
    void MenuChooseDB()
    {
        final FileChooser choose_file = new FileChooser();
        choose_file.setTitle("выберите базу со словами");
        choose_file.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All text files", "*.txt")
        );


        DataFile = choose_file.showOpenDialog(RootPane.getScene().getWindow());

        if (DataFile.exists())  setTitle(DataFile.getName());

        ReadToDB(DataFile);

        property.setProperty("DataBase",DataFile.getAbsolutePath());

        txtTarget.setText("あ");
        txtHint.setText("нажмите левую кнопку мыши, чтобы начать!");
        index = 0;
        txtTarget.setMouseTransparent(false);
        txtHint.setMouseTransparent(false);
    }
    @FXML
    void setTitle(String path)
    {
        ((Stage)RootPane.getScene().getWindow()).setTitle("にやあ　Dictator　[ "+ DataFile.getName() + " ]");
    }
    @FXML void WorkPlace_OnMouseClick(MouseEvent e)
    {
        //System.err.println("Mouse");

       if (e.getButton() == MouseButton.PRIMARY)
       {
           NextTask(false);
       }
       if (e.getButton() == MouseButton.SECONDARY)
           System.err.println("RMB");;
    }
    @FXML
    void  MenuSlide()
    {
        Double addSizeT = DefaultFontSizeTarget / 100 * MenuItem_Slider.getValue();
        Double addSizeH = DefaultFontSizeHint / 100 * MenuItem_Slider.getValue();
        Font fT = new Font(txtTarget.getFont().getFamily(),DefaultFontSizeTarget+addSizeT);
        Font fH = new Font(txtHint.getFont().getFamily(),DefaultFontSizeHint+addSizeH);

        txtTarget.setFont(fT);
        txtHint.setFont(fH);
    }
    @FXML
    void WorkPlace_OnMouseScroll(ScrollEvent e)
    {
        if (e.getDeltaY()>0)
        {
            MenuItem_Slider.setValue(MenuItem_Slider.getValue()+ 1);
            MenuSlide();
        }
        else if (e.getDeltaY() < 0)
        {
            MenuItem_Slider.setValue(MenuItem_Slider.getValue()- 1);
            MenuSlide();
        }
    }
    @FXML
    void MenuNewTry()
    {
        NextTask(true);
    }
    @FXML
    void ShowAbout()
    {
        try {
            FXMLLoader loadAbout = new FXMLLoader(getClass().getResource("/sample/About.fxml"));
            Parent abt = loadAbout.load();
            Stage abtWindow = new Stage(StageStyle.UTILITY);
            abtWindow.setScene(new Scene(abt, 339, 301));
            abtWindow.initModality(Modality.WINDOW_MODAL);
            abtWindow.setIconified(false);
            abtWindow.showAndWait();
        }
        catch (IOException e)
        {
            System.err.println("Не могу вызвать модальное окно.");
        }

    }


    /* NOT FXML*/
    void ReadToDB(File file)
    {
        DBnya.clear();
        DBword.clear();
        String[] buff;
        Long LinesCount = 0L;
        String one = "";
        String two = "";
        try
        {
             LinesCount = Files.lines(file.toPath()).count();

        }
        catch (IOException e)
        {
            System.err.println("Не могу посчитать строки в файле!");
        }

        try
        {
            System.out.println("Загружаем базу: " + LinesCount + " строк");
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
            String line = reader.readLine(); MenuProgressBar.setProgress(1 / LinesCount);

            while (line != null)
            {
                buff = line.trim().split("\\s+");
                if (buff.length==2)
                {
                    one = buff[0];
                    two = buff[1];
                }
                else
                {
                    one = buff[0];
                    for (int i = 1; i<buff.length; i++)
                        two +=" " + buff[i];
                }

                if(DBnya.contains(one) || DBword.contains(two))
                    System.err.println("Сочетание: " + one + " " + two + " уже было добавлено - пропускаем.");
                else {
                    DBnya.add(one);
                    DBword.add(two);
                    one = "";
                    two = "";
                    MenuProgressBar.setProgress(DBnya.size() / LinesCount);
                }

                System.out.println(DBnya.get(DBnya.size()-1)+" " +DBword.get(DBword.size()-1)+" добавлено");
                line = reader.readLine();
            }
        }
        catch(FileNotFoundException e)
        {
            System.err.println("Файл не был найден!");
        }
        catch (IOException e)
        {
            System.err.println("IOException");
        }
    }

    public boolean NextTask(boolean begin)
    {

        if (begin)
        {
            index = 0;
            txtTarget.setText("あ");
            txtHint.setText("нажмите левую кнопку мыши, чтобы начать");
            MenuProgressBar.setProgress(0);
            return true;
        }
        if (index == DBnya.size())
        {
            System.err.println("Диктант завершен!");
            txtTarget.setText("Поздравляем!");
            txtHint.setText("вы успешно дошли до конца");

            hint = false;

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Диктовка завершена");
            alert.setHeaderText("Диктовка завершена! Хотите начать заново?");
            alert.setContentText("Нажмите \"Да\", чтобы начать заново. Если хотите начать диктовку с другой базой, " +
                    "нажмите \"Нет\" и выберите новую базу из меню.");

            ButtonType btYes = new ButtonType("Да");
            ButtonType btNo = new ButtonType("Нет", ButtonBar.ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(btYes,btNo);

            Optional<ButtonType> result = alert.showAndWait();

            if (result.get() == btYes)
            {
                index = 0;
                txtTarget.setText("あ");
                txtHint.setText("Нажмите левую кнопку мыши, чтобы начать");;
                MenuProgressBar.setProgress(0);
            }
            else
            {

            }

            return false;
        }
        else if (DBnya.size()> 0 && DBword.size()> 0)
        {
            if (!hint)
            {
                txtHint.setText("");
                 if (mode.compareTo("read")==0) txtTarget.setText(DBnya.get(index));
                 else txtTarget.setText(DBword.get(index));

                hint = true;
                return true;
            }
            else
            {
                if (mode.compareTo("read")==0) txtHint.setText(DBword.get(index));
                else txtHint.setText(DBnya.get(index));
                hint = false;
                index++;
                MenuProgressBar.setProgress((double)index / DBnya.size());
                return true;
            }

        }
        else
        {
            System.err.println("База не загружена!");
            return false;
        }
    }
}
