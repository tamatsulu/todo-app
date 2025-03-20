package com.example.todoapp.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.todoapp.model.Category;
import com.example.todoapp.model.Todo;
import com.example.todoapp.repository.CategoryRepository;
import com.example.todoapp.repository.TodoRepository;

import jakarta.annotation.PostConstruct;

@RestController
@RequestMapping("/todos")
public class TodoController {
    private final TodoRepository todoRepository;
    private final CategoryRepository categoryRepository;

    public TodoController(TodoRepository todoRepository, CategoryRepository categoryRepository) {
        this.todoRepository = todoRepository;
        this.categoryRepository = categoryRepository;
    }

    // 初期カテゴリをデータベースに保存
    @PostConstruct
    public void initCategories() {
        String[] initialCategories = {"今日やるべきこと", "仕事", "買い物", "アメリカ旅行"};
        String[] initialColors = {"#ffcccc", "#ccccff", "#ccffcc", "#ffebcc"}; // 初期カテゴリの背景色
        for (int i = 0; i < initialCategories.length; i++) {
            String categoryName = initialCategories[i];
            if (categoryRepository.findByName(categoryName) == null) {
                categoryRepository.save(new Category(categoryName, initialColors[i]));
            }
        }
    }

    // すべてのカテゴリを取得（背景色を含む）
    @GetMapping("/categories")
    public List<Category> getCategories() {
        return categoryRepository.findAll();
    }

    // 新しいカテゴリを追加（背景色を含む）
    @PostMapping("/categories")
    public void addCategory(@RequestBody Category category) {
        String cleanedCategoryName = category.getName().replaceAll("^\"|\"$", "");
        if (categoryRepository.findByName(cleanedCategoryName) == null) {
            category.setName(cleanedCategoryName);
            if (category.getBackgroundColor() == null) {
                category.setBackgroundColor("#ffffff"); // デフォルトは白色
            }
            categoryRepository.save(category);
        }
    }

    // カテゴリの背景色を更新
    @PutMapping("/categories/{category}/color")
    public void updateCategoryColor(@PathVariable String category, @RequestBody String backgroundColor) {
        Category categoryEntity = categoryRepository.findByName(category);
        if (categoryEntity != null) {
            String cleanedColor = backgroundColor.replaceAll("^\"|\"$", "");
            categoryEntity.setBackgroundColor(cleanedColor);
            categoryRepository.save(categoryEntity);
        }
    }

    // カテゴリを削除（関連するタスクも削除）
    @DeleteMapping("/categories/{category}")
    public void deleteCategory(@PathVariable String category) {
        // 関連するタスクを削除
        List<Todo> todos = todoRepository.findByCategory(category);
        todos.forEach(todo -> todoRepository.delete(todo));
        // カテゴリを削除
        Category categoryEntity = categoryRepository.findByName(category);
        if (categoryEntity != null) {
            categoryRepository.delete(categoryEntity);
        }
    }

    // すべてのタスクを取得
    @GetMapping
    public List<Todo> getAllTodos() {
        return todoRepository.findAll();
    }

    // カテゴリごとにタスクを取得
    @GetMapping("/category/{category}")
    public List<Todo> getTodosByCategory(@PathVariable String category) {
        return todoRepository.findByCategory(category);
    }

    // タスクを作成
    @PostMapping
    public Todo createTodo(@RequestBody Todo todo) {
        if (categoryRepository.findByName(todo.getCategory()) == null) {
            categoryRepository.save(new Category(todo.getCategory()));
        }
        return todoRepository.save(todo);
    }

    // 特定タスクを取得
    @GetMapping("/{id}")
    public Todo getTodoById(@PathVariable Long id) {
        return todoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Todo not found"));
    }

    // タスクを更新
    @PutMapping("/{id}")
    public Todo updateTodo(@PathVariable Long id, @RequestBody Todo updatedTodo) {
        Todo todo = getTodoById(id);
        todo.setTitle(updatedTodo.getTitle());
        todo.setCompleted(updatedTodo.isCompleted());
        todo.setCategory(updatedTodo.getCategory());
        if (categoryRepository.findByName(updatedTodo.getCategory()) == null) {
            categoryRepository.save(new Category(updatedTodo.getCategory()));
        }
        return todoRepository.save(todo);
    }

    // タスクを削除
    @DeleteMapping("/{id}")
    public void deleteTodo(@PathVariable Long id) {
        todoRepository.deleteById(id);
    }
}