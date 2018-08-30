package controllers;

import models.discussions.DiscussionCategory;
import models.serializer.DiscussionCategorySerializer;
import play.db.jpa.Transactional;

import java.util.List;

/**
 * Created by Jonathan on 14/10/2015.
 */
public class CategoriesController extends Application {

    @Transactional(readOnly = true)
    public static void getAll() {
        if(Security.isConnected() && Security.isAdmin()) {
            List<DiscussionCategory> categories = DiscussionCategory.findAllOrdered();
            renderJSON(categories, DiscussionCategorySerializer.get());
        } else {
            forbidden();
        }
    }

    @Transactional(readOnly = true)
    public static void manage(Long id) {
        if(Security.isConnected() && Security.isAdmin()) {
            render();
        } else {
            forbidden();
        }
    }

    @Transactional()
    public static void createCategory(String label) {
        if(Security.isConnected() && Security.isAdmin()) {
            DiscussionCategory category = new DiscussionCategory();
            category.setLabel(label);
            category.save();
            ok();
        } else {
            forbidden();
        }
    }

    @Transactional()
    public static void deleteCategory(Long id) {
        if(Security.isConnected() && Security.isAdmin()) {
            DiscussionCategory category = DiscussionCategory.findById(id);
            category.delete();
            ok();
        } else {
            forbidden();
        }
    }

}
