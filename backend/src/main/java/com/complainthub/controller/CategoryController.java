package com.complainthub.controller;

import com.complainthub.entity.Category;
import com.complainthub.entity.enums.UserRole;
import com.complainthub.service.CategoryService;
import com.complainthub.service.CategoryServiceImpl;
import com.complainthub.util.AuthorizationUtil;
import com.complainthub.util.ResponseUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/categories/*")
public class CategoryController extends HttpServlet {

    private final CategoryService categoryService =
            new CategoryServiceImpl();

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse res
    ) throws ServletException, IOException {

        try {
            String pathInfo =
                    req.getPathInfo();

            /*
             * GET /api/categories
             *
             * Returns only active categories.
             */
            if (pathInfo == null
                    || pathInfo.equals("/")) {

                List<Category> categories =
                        categoryService.getActiveCategories();

                writeCategoryList(
                        categories,
                        res,
                        "No active categories found.",
                        "Active categories retrieved successfully."
                );

                return;
            }

            /*
             * GET /api/categories/all
             *
             * Returns all categories, including
             * deactivated categories.
             *
             * ADMIN only.
             */
            if (pathInfo.equals("/all")) {

                AuthorizationUtil.requireRole(
                        req,
                        UserRole.ADMIN
                );

                List<Category> categories =
                        categoryService.getAllCategories();

                writeCategoryList(
                        categories,
                        res,
                        "No categories found.",
                        "Categories retrieved successfully."
                );

                return;
            }

            /*
             * GET /api/categories/{id}
             *
             * Returns only active categories.
             */
            long categoryId =
                    parseId(pathInfo);

            Category category =
                    categoryService.getCategoryById(
                            categoryId
                    );

            if (category == null
                    || !category.isActive()) {

                ResponseUtil.sendError(
                        res,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Category not found."
                );

                return;
            }

            ResponseUtil.sendSuccess(
                    res,
                    HttpServletResponse.SC_OK,
                    "Category retrieved successfully.",
                    toCategoryResponse(category)
            );

        } catch (SecurityException e) {

            ResponseUtil.sendError(
                    res,
                    HttpServletResponse.SC_FORBIDDEN,
                    "Access denied: " + e.getMessage()
            );

        } catch (IllegalArgumentException e) {

            ResponseUtil.sendError(
                    res,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid request: " + e.getMessage()
            );

        } catch (Exception e) {

            getServletContext().log(
                    "Unable to process category request.",
                    e
            );

            ResponseUtil.sendError(
                    res,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to process category request."
            );
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse res
    ) throws ServletException, IOException {

        try {
            AuthorizationUtil.requireRole(
                    req,
                    UserRole.ADMIN
            );

            String name =
                    req.getParameter("name");

            String description =
                    req.getParameter("description");

            Category category =
                    new Category();

            category.setName(name);
            category.setDescription(description);

            Category createdCategory =
                    categoryService.createCategory(
                            category
                    );

            ResponseUtil.sendSuccess(
                    res,
                    HttpServletResponse.SC_CREATED,
                    "Category created successfully.",
                    toCategoryResponse(
                            createdCategory
                    )
            );

        } catch (SecurityException e) {

            ResponseUtil.sendError(
                    res,
                    HttpServletResponse.SC_FORBIDDEN,
                    "Access denied: " + e.getMessage()
            );

        } catch (IllegalArgumentException e) {

            ResponseUtil.sendError(
                    res,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Category creation failed: "
                            + e.getMessage()
            );

        } catch (Exception e) {

            getServletContext().log(
                    "Unable to create category.",
                    e
            );

            ResponseUtil.sendError(
                    res,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to create category."
            );
        }
    }

    @Override
    protected void doPut(
            HttpServletRequest req,
            HttpServletResponse res
    ) throws ServletException, IOException {

        try {
            AuthorizationUtil.requireRole(
                    req,
                    UserRole.ADMIN
            );

            String pathInfo =
                    req.getPathInfo();

            long categoryId =
                    parseId(pathInfo);

            String name =
                    req.getParameter("name");

            String description =
                    req.getParameter("description");

            Category category =
                    categoryService.getCategoryById(
                            categoryId
                    );

            if (category == null) {

                ResponseUtil.sendError(
                        res,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Category not found."
                );

                return;
            }

            category.setName(name);
            category.setDescription(description);

            Category updatedCategory =
                    categoryService.updateCategory(
                            category
                    );

            ResponseUtil.sendSuccess(
                    res,
                    HttpServletResponse.SC_OK,
                    "Category updated successfully.",
                    toCategoryResponse(
                            updatedCategory
                    )
            );

        } catch (SecurityException e) {

            ResponseUtil.sendError(
                    res,
                    HttpServletResponse.SC_FORBIDDEN,
                    "Access denied: " + e.getMessage()
            );

        } catch (IllegalArgumentException e) {

            ResponseUtil.sendError(
                    res,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Category update failed: "
                            + e.getMessage()
            );

        } catch (Exception e) {

            getServletContext().log(
                    "Unable to update category.",
                    e
            );

            ResponseUtil.sendError(
                    res,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to update category."
            );
        }
    }

    @Override
    protected void doDelete(
            HttpServletRequest req,
            HttpServletResponse res
    ) throws ServletException, IOException {

        try {
            AuthorizationUtil.requireRole(
                    req,
                    UserRole.ADMIN
            );

            String pathInfo =
                    req.getPathInfo();

            long categoryId =
                    parseId(pathInfo);

            boolean deactivated =
                    categoryService.deactivateCategory(
                            categoryId
                    );

            if (!deactivated) {

                ResponseUtil.sendError(
                        res,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Category not found."
                );

                return;
            }

            Map<String, Object> data =
                    new LinkedHashMap<>();

            data.put(
                    "categoryId",
                    categoryId
            );

            data.put(
                    "active",
                    false
            );

            ResponseUtil.sendSuccess(
                    res,
                    HttpServletResponse.SC_OK,
                    "Category deactivated successfully.",
                    data
            );

        } catch (SecurityException e) {

            ResponseUtil.sendError(
                    res,
                    HttpServletResponse.SC_FORBIDDEN,
                    "Access denied: " + e.getMessage()
            );

        } catch (IllegalArgumentException e) {

            ResponseUtil.sendError(
                    res,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Category deactivation failed: "
                            + e.getMessage()
            );

        } catch (Exception e) {

            getServletContext().log(
                    "Unable to deactivate category.",
                    e
            );

            ResponseUtil.sendError(
                    res,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to deactivate category."
            );
        }
    }

    private void writeCategoryList(
            List<Category> categories,
            HttpServletResponse response,
            String emptyMessage,
            String successMessage
    ) throws IOException {

        List<Map<String, Object>> categoryResponses =
                new ArrayList<>();

        if (categories != null) {

            for (Category category : categories) {

                categoryResponses.add(
                        toCategoryResponse(
                                category
                        )
                );
            }
        }

        Map<String, Object> data =
                new LinkedHashMap<>();

        data.put(
                "total",
                categoryResponses.size()
        );

        data.put(
                "categories",
                categoryResponses
        );

        ResponseUtil.sendSuccess(
                response,
                HttpServletResponse.SC_OK,
                categoryResponses.isEmpty()
                        ? emptyMessage
                        : successMessage,
                data
        );
    }

    private Map<String, Object> toCategoryResponse(
            Category category
    ) {

        Map<String, Object> data =
                new LinkedHashMap<>();

        data.put(
                "id",
                category.getId()
        );

        data.put(
                "name",
                category.getName()
        );

        data.put(
                "description",
                category.getDescription()
        );

        data.put(
                "active",
                category.isActive()
        );

        return data;
    }

    private long parseId(
            String pathInfo
    ) {

        if (pathInfo == null
                || pathInfo.equals("/")
                || pathInfo.length() <= 1) {

            throw new IllegalArgumentException(
                    "Category ID is required."
            );
        }

        String idValue =
                pathInfo.substring(1);

        if (idValue.contains("/")) {

            throw new IllegalArgumentException(
                    "Invalid category ID."
            );
        }

        try {

            long id =
                    Long.parseLong(idValue);

            if (id <= 0) {

                throw new IllegalArgumentException(
                        "Category ID must be greater than zero."
                );
            }

            return id;

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    "Category ID must be a valid number."
            );
        }
    }
}