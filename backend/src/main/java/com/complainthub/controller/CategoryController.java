package com.complainthub.controller;

import com.complainthub.entity.Category;
import com.complainthub.entity.enums.UserRole;
import com.complainthub.service.CategoryService;
import com.complainthub.service.CategoryServiceImpl;
import com.complainthub.util.AuthorizationUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/api/categories/*")
public class CategoryController extends HttpServlet {

    private final CategoryService categoryService = new CategoryServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        res.setContentType("text/plain");
        res.setCharacterEncoding("UTF-8");

        try {
            String pathInfo = req.getPathInfo();

            // GET /api/categories
            // Returns only active categories
            if (pathInfo == null || pathInfo.equals("/")) {
                List<Category> categories = categoryService.getActiveCategories();

                res.setStatus(HttpServletResponse.SC_OK);
                for (Category category : categories) {
                    res.getWriter().println(
                            "ID: " + category.getId()
                                    + ", Name: " + category.getName()
                                    + ", Description: " + category.getDescription()
                    );
                }

                return;
            }

            // GET /api/categories/all
            // Returns all categories, including deactivated categories.
            // ADMIN only.
            if (pathInfo.equals("/all")) {
                AuthorizationUtil.requireRole(req, UserRole.ADMIN);

                List<Category> categories = categoryService.getAllCategories();

                res.setStatus(HttpServletResponse.SC_OK);
                for (Category category : categories) {
                    res.getWriter().println(
                            "ID: " + category.getId()
                                    + ", Name: " + category.getName()
                                    + ", Description: " + category.getDescription()
                                    + ", Active: " + category.isActive()
                    );
                }

                return;
            }

            // GET /api/categories/{id}
            // Returns only active categories.
            long categoryId = parseId(pathInfo);

            Category category = categoryService.getCategoryById(categoryId);
            if (category == null || !category.isActive()) {
                res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                res.getWriter().println("Category not found.");
                return;
            }

            res.setStatus(HttpServletResponse.SC_OK);
            res.getWriter().println("ID: " + category.getId());
            res.getWriter().println("Name: " + category.getName());
            res.getWriter().println("Description: " + category.getDescription());
            res.getWriter().println("Active: " + category.isActive());

        } catch (SecurityException e) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            res.getWriter().println("Access denied: " + e.getMessage());

        } catch (IllegalArgumentException e) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().println("Invalid request: " + e.getMessage());

        } catch (Exception e) {
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            res.getWriter().println("Unable to process category request.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        res.setContentType("text/plain");
        res.setCharacterEncoding("UTF-8");

        try {
            AuthorizationUtil.requireRole(req, UserRole.ADMIN);

            String name = req.getParameter("name");
            String description = req.getParameter("description");

            Category category = new Category();
            category.setName(name);
            category.setDescription(description);

            Category createdCategory = categoryService.createCategory(category);

            res.setStatus(HttpServletResponse.SC_CREATED);
            res.getWriter().println("Category created successfully.");
            res.getWriter().println("Category ID: " + createdCategory.getId());
            res.getWriter().println("Category Name: " + createdCategory.getName());
            res.getWriter().println("Category Description: " + createdCategory.getDescription());

        } catch (SecurityException e) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            res.getWriter().println("Access denied: " + e.getMessage());

        } catch (IllegalArgumentException e) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().println("Category creation failed: " + e.getMessage());

        } catch (Exception e) {
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            res.getWriter().println("Unable to create category.");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        res.setContentType("text/plain");
        res.setCharacterEncoding("UTF-8");

        try {
            AuthorizationUtil.requireRole(req, UserRole.ADMIN);

            String pathInfo = req.getPathInfo();
            long categoryId = parseId(pathInfo);

            String name = req.getParameter("name");
            String description = req.getParameter("description");

            Category category = categoryService.getCategoryById(categoryId);
            if (category == null) {
                res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                res.getWriter().println("Category not found.");
                return;
            }

            category.setName(name);
            category.setDescription(description);

            Category updatedCategory = categoryService.updateCategory(category);

            res.setStatus(HttpServletResponse.SC_OK);
            res.getWriter().println("Category updated successfully.");
            res.getWriter().println("Category ID: " + updatedCategory.getId());
            res.getWriter().println("Category Name: " + updatedCategory.getName());
            res.getWriter().println("Category Description: " + updatedCategory.getDescription());

        } catch (SecurityException e) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            res.getWriter().println("Access denied: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().println("Category update failed: " + e.getMessage());
        } catch (Exception e) {
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            res.getWriter().println("Unable to update category.");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        res.setContentType("text/plain");
        res.setCharacterEncoding("UTF-8");

        try {
            AuthorizationUtil.requireRole(req, UserRole.ADMIN);

            String pathInfo = req.getPathInfo();
            long categoryId = parseId(pathInfo);

            boolean deactivated = categoryService.deactivateCategory(categoryId);
            if (!deactivated) {
                res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                res.getWriter().println("Category not found.");
                return;
            }

            res.setStatus(HttpServletResponse.SC_OK);
            res.getWriter().println("Category deactivated successfully.");
            res.getWriter().println("Category ID: " + categoryId);

        } catch (SecurityException e) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            res.getWriter().println("Access denied: " + e.getMessage());

        } catch (IllegalArgumentException e) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().println("Category deactivation failed: " + e.getMessage());

        } catch (Exception e) {
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            res.getWriter().println("Unable to deactivate category.");
        }
    }

    private long parseId(String pathInfo) {
        if (pathInfo == null || pathInfo.equals("/") || pathInfo.length() <= 1) {
            throw new IllegalArgumentException("Category ID is required.");
        }

        String idValue = pathInfo.substring(1);
        if (idValue.contains("/")) {
            throw new IllegalArgumentException("Invalid category ID.");
        }

        try {
            return Long.parseLong(idValue);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Category ID must be a valid number.");
        }
    }
}