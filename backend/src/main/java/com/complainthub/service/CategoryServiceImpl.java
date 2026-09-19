package com.complainthub.service;

import com.complainthub.dao.CategoryDao;
import com.complainthub.entity.Category;
import com.complainthub.service.CategoryService;
import com.complainthub.util.ValidationUtil;

import java.util.List;

public class CategoryServiceImpl implements CategoryService{
    private final CategoryDao categoryDao;
    public CategoryServiceImpl(){
        this.categoryDao = new CategoryDao();
    }

    @Override
    public Category createCategory(Category category) {
        validateCategory(category);

        category.setName(category.getName().trim());

        if(category.getDescription() != null)
            category.setDescription(category.getDescription().trim());

        category.setActive(true);
        categoryDao.save(category);
        return category;
    }

    @Override
    public Category getCategoryById(long id) {
        ValidationUtil.validateId(id, "Category Id");

        return categoryDao.findById(id);
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryDao.findAll();
    }

    @Override
    public List<Category> getActiveCategories() {
        return categoryDao.findAllActive();
    }

    @Override
    public Category updateCategory(Category category) {
        validateCategory(category);
        ValidationUtil.validateId(category.getId(), "Category Id");

        Category oldCategory = categoryDao.findById(category.getId());
        if(oldCategory == null)
            throw new IllegalArgumentException("Category not found.");

        category.setName(category.getName().trim());

        if(category.getDescription() != null)
            category.setDescription(category.getDescription().trim());

        categoryDao.update(category);
        return category;
    }

    @Override
    public boolean activateCategory(long id) {
        ValidationUtil.validateId(id, "Category Id");

        Category category = categoryDao.findById(id);
        if(category == null)
            return false;

        if(category.isActive()) {
            return true;
        }

        categoryDao.activate(id);
        return true;
    }

    @Override
    public boolean deactivateCategory(long id) {
        ValidationUtil.validateId(id, "Category Id");

        Category category = categoryDao.findById(id);
        if(category == null)
            return false;

        if(!category.isActive()) {
            return true;
        }

        categoryDao.deactivate(id);
        return true;
    }

    // -------- Validate Method --------
    private void validateCategory(Category category){
        if(category == null)
            throw new IllegalArgumentException("Category cannot be null.");


        ValidationUtil.validateRequired(category.getName(), "Category Name");

        ValidationUtil.validateMaxLength(category.getName().trim(), 100, "Category Name");

        if (category.getDescription() != null) {
            ValidationUtil.validateMaxLength(
                    category.getDescription().trim(),
                    500,
                    "Category Description"
            );
        }
    }
}
