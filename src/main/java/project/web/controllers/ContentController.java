package project.web.controllers;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import org.primefaces.event.FileUploadEvent;

@ManagedBean
@SessionScoped
public class ContentController implements Serializable {

    private String fileName;
    @ManagedProperty(value = "#{bookListController}")
    private BookListController bookListController;

    public BookListController getBookListController() {
        return bookListController;
    }

    public void setBookListController(BookListController bookListController) {
        this.bookListController = bookListController;
    }

    public void handleFileUpload(FileUploadEvent event) {
        bookListController.getSelectedBook().setUploadedContent(event.getFile().getContents());
        fileName = event.getFile().getFileName();
    }

    public String getFileName() {
        if (fileName!=null && fileName.length() > 20) {
            return new StringBuilder().append(fileName.substring(0, 19)).append("...").toString();
        }
        return fileName;
    }

    public boolean isShowContent() {
        if (bookListController.getSelectedBook().getUploadedContent() != null || bookListController.getSelectedBook().getContent()!=null) {
            return true;
        }

        return false;
    }

    public byte[] getUploadedContent() {
        return bookListController.getSelectedBook().getUploadedContent();
    }
}
