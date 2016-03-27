package project.web.controllers;

import java.io.Serializable;
import java.util.Map;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import org.primefaces.component.datagrid.DataGrid;
import org.primefaces.context.RequestContext;
import org.primefaces.model.LazyDataModel;
import project.web.db.DataHelper;
import project.web.entity.ext.BookExt;
import project.web.enums.SearchType;
import project.web.models.BookListDataModel;
import project.web.beans.Pager;

@ManagedBean(eager = true)
@SessionScoped
public class BookListController implements Serializable {

    private SessionUtils sessionUtils = SessionUtils.getInstance();
    
    private DataGrid dataTable;
    private BookExt selectedBook;
    private BookExt newBook;
    private transient DataHelper dataHelper;
    private LazyDataModel<BookExt> bookListModel;
    private Long selectedAuthorId;// текущий автор книги из списка при редактировании книги
    // критерии поиска
    private char selectedLetter; // выбранная буква алфавита, по умолчанию не выбрана ни одна буква
    private SearchType selectedSearchType = SearchType.TITLE;// хранит выбранный тип поиска, по-умолчанию - по названию
    private long selectedGenreId; // выбранный жанр
    private String currentSearchString; // хранит поисковую строку
    private Pager pager;
    //-------
    private boolean editMode;// отображение режима редактирования
    private boolean addMode;// отображение режима добавление

    public BookListController() {
        pager = new Pager();
        dataHelper = new DataHelper(pager);
        bookListModel = new BookListDataModel(dataHelper, pager);
    }

    public DataHelper getDataHelper() {
        return dataHelper;
    }

    public Pager getPager() {
        return pager;
    }

    private void submitValues(Character selectedLetter, long selectedGenreId) {
        this.selectedLetter = selectedLetter;
        this.selectedGenreId = selectedGenreId;
        dataTable.setFirst(0);
    }

    //<editor-fold defaultstate="collapsed" desc="запросы в базу">
    private void fillBooksAll() {
        dataHelper.getAllBooks();
    }

    public void fillBooksByGenre() {

        imitateLoading();

        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        selectedGenreId = Long.valueOf(params.get("genre_id"));
        submitValues(' ', selectedGenreId);
        dataHelper.getBooksByGenre(selectedGenreId);

    }

    public void fillBooksByRate() {

        imitateLoading();
        dataHelper.getBooksByRate();

    }

    public void fillBooksByLetter() {

        imitateLoading();

        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        selectedLetter = params.get("letter").charAt(0);
        submitValues(selectedLetter, -1);
        dataHelper.getBooksByLetter(selectedLetter);

    }

    public void fillBooksBySearch() {

        imitateLoading();

        submitValues(' ', -1);

        if (currentSearchString.trim().length() == 0) {
            fillBooksAll();

        }

        if (selectedSearchType == SearchType.AUTHOR) {
            dataHelper.getBooksByAuthor(currentSearchString);
        } else if (selectedSearchType == SearchType.TITLE) {
            dataHelper.getBooksByName(currentSearchString);
        }

    }

    public void deleteBook() {
        dataHelper.deleteBook(selectedBook);
        dataHelper.populateList();

//        RequestContext.getCurrentInstance().execute("dlgDeleteBook.hide()");
        
        sessionUtils.showMessage("deleted");
        
        dataTable.setFirst(calcSelectedPage());

    }

    public void rate() {
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        int bookIndex = Integer.parseInt(params.get("bookIndex"));

        BookExt book = pager.getList().get(bookIndex);

        dataHelper.rateBook(book, sessionUtils.getUserName());

    }

    public void saveBook() {

        if (!validateFields()) {
            return;
        }

        if (editMode) {
            dataHelper.updateBook(selectedBook);
        } else if (addMode) {
            dataHelper.addBook(selectedBook.getBook());
        }

        cancelModes();
        dataHelper.populateList();
        
        sessionUtils.showMessage("updated");

        dataTable.setFirst(calcSelectedPage());
    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="режим редактирования">
    public void switchEditMode() {
        editMode = true;
        RequestContext.getCurrentInstance().execute("dlgEditBook.show()");

    }

    public void switchAddMode() {
        addMode = true;
        selectedBook = new BookExt();
        selectedBook.setGenre(dataHelper.getGenre(selectedGenreId));
        RequestContext.getCurrentInstance().execute("dlgEditBook.show()");

    }
    
  

    public void cancelModes() {
        if (addMode) {
            addMode = false;
        }

        if (editMode) {
            editMode = false;
        }

        if (selectedBook != null) {
            selectedBook.setUploadedContent(null);
            selectedBook.setUploadedImage(null);
        }

        RequestContext.getCurrentInstance().execute("dlgEditBook.hide()");
    }

    //</editor-fold>
    public Character[] getRussianLetters() {
        Character[] letters = new Character[]{'А', 'Б', 'В', 'Г', 'Д', 'Е', 'Ё', 'Ж', 'З', 'И', 'Й', 'К', 'Л', 'М', 'Н', 'О', 'П', 'Р', 'С', 'Т', 'У', 'Ф', 'Х', 'Ц', 'Ч', 'Ш', 'Щ', 'Ъ', 'Ы', 'Ь', 'Э', 'Ю', 'Я',};
        return letters;
    }

    public void searchStringChanged(ValueChangeEvent e) {
        currentSearchString = e.getNewValue().toString();
    }

    public void searchTypeChanged(ValueChangeEvent e) {
        selectedSearchType = (SearchType) e.getNewValue();
    }

    private int calcSelectedPage() {
        int page = dataTable.getPage();// текущий номер страницы (индексация с нуля)

        int leftBound = pager.getTo() * (page - 1);
        int rightBound = pager.getTo() * page;

        boolean goPrevPage = pager.getTotalBooksCount() > leftBound & pager.getTotalBooksCount() <= rightBound;


        if (goPrevPage) {
            page--;
        }

        return (page > 0) ? page * pager.getTo() : 0;
    }

    //<editor-fold defaultstate="collapsed" desc="гетеры сетеры">
    public boolean isEditMode() {
        return editMode;
    }

    public boolean isAddMode() {
        return addMode;
    }

    public String getSearchString() {
        return currentSearchString;
    }

    public void setSearchString(String searchString) {
        this.currentSearchString = searchString;
    }

    public SearchType getSearchType() {
        return selectedSearchType;
    }

    public void setSearchType(SearchType searchType) {
        this.selectedSearchType = searchType;
    }

    public long getSelectedGenreId() {
        return selectedGenreId;
    }

    public void setSelectedGenreId(int selectedGenreId) {
        this.selectedGenreId = selectedGenreId;
    }

    public char getSelectedLetter() {
        return selectedLetter;
    }

    public void setSelectedLetter(char selectedLetter) {
        this.selectedLetter = selectedLetter;
    }

    public Long getSelectedAuthorId() {
        return selectedAuthorId;
    }

    public void setSelectedAuthorId(Long selectedAuthorId) {
        this.selectedAuthorId = selectedAuthorId;
    }

    public LazyDataModel<BookExt> getBookListModel() {
        return bookListModel;
    }

    public void setSelectedBook(BookExt selectedBook) {
        this.selectedBook = selectedBook;
    }

    public BookExt getSelectedBook() {
        return selectedBook;
    }

    public DataGrid getDataGrid() {
        return dataTable;
    }

    public void setDataGrid(DataGrid dataTable) {
        this.dataTable = dataTable;
    }

    public BookExt getNewBook() {
        if (newBook == null) {
            newBook = new BookExt();
        }
        return newBook;
    }

    public void setNewBook(BookExt newBook) {
        this.newBook = newBook;
    }

    //</editor-fold>
    private void imitateLoading() {

    }

    private boolean validateFields() {

        if (sessionUtils.isNullOrEmpty(selectedBook.getAuthor())
                || sessionUtils.isNullOrEmpty(selectedBook.getDescr())
                || sessionUtils.isNullOrEmpty(selectedBook.getGenre())
                || sessionUtils.isNullOrEmpty(selectedBook.getIsbn())
                || sessionUtils.isNullOrEmpty(selectedBook.getName())
                || sessionUtils.isNullOrEmpty(selectedBook.getPageCount())
                || sessionUtils.isNullOrEmpty(selectedBook.getPublishYear())
                || sessionUtils.isNullOrEmpty(selectedBook.getPublisher())) {
            sessionUtils.failValidation("error_fill_all_fields");
            return false;

        }
        
        
        if (dataHelper.isIsbnExist(selectedBook.getIsbn(), selectedBook.getId())){
            sessionUtils.failValidation("error_isbn_exist");
            return false;            
        }

        if (addMode) {

            if (selectedBook.getUploadedContent() == null) {
                sessionUtils.failValidation("error_load_pdf");
                return false;
            }

            if (selectedBook.getUploadedImage() == null) {
                sessionUtils.failValidation("error_load_image");
                return false;
            }

        }


        return true;


    }

   

   
}
