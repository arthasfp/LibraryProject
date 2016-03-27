package project.web.controllers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.model.SelectItem;

import project.web.comparators.ListComparator;
import project.web.entity.ext.AuthorExt;
import project.web.beans.Pager;
import project.web.db.DataHelper;

@ManagedBean
@SessionScoped
public class AuthorController implements Serializable, Converter {

    private List<SelectItem> selectItems = new ArrayList<SelectItem>();
    private Map<Long, AuthorExt> map;
    private List<AuthorExt> list;
//    private final BookListController bookListController;
    private Pager pager;
    private DataHelper dataHelper;
    @ManagedProperty(value = "#{bookListController}")
    private BookListController bookListController;

    @PostConstruct
    public void init() {
        pager = bookListController.getPager();
        dataHelper = bookListController.getDataHelper();

        map = new HashMap<Long, AuthorExt>();
        list = dataHelper.getAllAuthors();
        Collections.sort(list, ListComparator.getInstance());
        
        list.add(0, createEmptyAuthor());

        for (AuthorExt author : list) {
            map.put(author.getId(), author);
            selectItems.add(new SelectItem(author, author.getFio()));
        }
    }

  
    public List<AuthorExt> getAuthorList() {
        return list;
    }

    public List<SelectItem> getSelectItems() {
        return selectItems;
    }

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        return map.get(Long.valueOf(value));
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        return ((AuthorExt) value).getId().toString();
    }

    public BookListController getBookListController() {
        return bookListController;
    }

    public void setBookListController(BookListController bookListController) {
        this.bookListController = bookListController;
    }

    private AuthorExt createEmptyAuthor() {
        AuthorExt author = new AuthorExt();
        author.setId(-1L);
        author.setFio("");
        return author;
    }
}
