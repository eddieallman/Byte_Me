package com.byteme.rescuebites;

import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepo;

    @GetMapping
    public List<Category> getAll() {
        return categoryRepo.findAll();
    }
}
