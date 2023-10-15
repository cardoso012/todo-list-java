package br.com.lucascardoso.todolist.task.controller;

import br.com.lucascardoso.todolist.task.model.TaskModel;
import br.com.lucascardoso.todolist.task.repository.ITaskRepository;
import br.com.lucascardoso.todolist.util.Utils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/tasks")
public class TaskController {

    @Autowired
    private ITaskRepository taskRepository;

    @PostMapping()
    public ResponseEntity create(@RequestBody TaskModel taskModel, HttpServletRequest request)
    {
        var idUser = request.getAttribute("idUser");
        taskModel.setIdUser((UUID) idUser);

        var currentDate = LocalDateTime.now();
        if(currentDate.isAfter(taskModel.getStartAt()) || currentDate.isAfter(taskModel.getEndAt())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A data de inicio/fim deve ser maior que a data atual");
        }

        if(taskModel.getStartAt().isAfter(taskModel.getEndAt())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A data de inicio deve ser menor que a data fim");
        }

        var taskCreated = this.taskRepository.save(taskModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(taskCreated);
    }

    @GetMapping()
    public List<TaskModel> list(HttpServletRequest request)
    {
        var idUser = request.getAttribute("idUser");
        return this.taskRepository.findByIdUser((UUID) idUser);
    }

    @PutMapping("/{id}")
    public ResponseEntity update(@RequestBody TaskModel taskModel, @PathVariable UUID id, HttpServletRequest request)
    {
        var task = this.taskRepository.findById(id).orElse(null);

        if(task == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tarefa não encontrada");
        }

        var idUser = request.getAttribute("idUser");
        if(!task.getIdUser().equals(idUser)){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não tem permissão para alterar esta tarefa!");
        }

        Utils.copyNonNullProperties(taskModel, task);

        var taskUpdated = this.taskRepository.save(task);
        return ResponseEntity.status(HttpStatus.OK).body(taskUpdated);
    }

}