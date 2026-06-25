package com.trabalho.verificadocs.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.trabalho.verificadocs.model.Analise;
import com.trabalho.verificadocs.model.Usuario;
import com.trabalho.verificadocs.service.AmbienteService;
import com.trabalho.verificadocs.service.AnaliseService;
import com.trabalho.verificadocs.service.UsuarioService;

@Controller
public class AnaliseController {

    private final AnaliseService analiseService;
    private final UsuarioService usuarioService;
    private final AmbienteService ambienteService;

    public AnaliseController(
            AnaliseService analiseService,
            UsuarioService usuarioService,
            AmbienteService ambienteService) {
        this.analiseService = analiseService;
        this.usuarioService = usuarioService;
        this.ambienteService = ambienteService;
    }

    @GetMapping("/analises/{id}")
    public String detalhe(
            @PathVariable Long id,
            Authentication authentication,
            Model model) {
        Usuario usuario = usuarioService.buscarUsuarioAutenticado(authentication);
        Analise analise = analiseService.buscarAnaliseDoUsuario(id, usuario);

        model.addAttribute("usuario", usuario);
        model.addAttribute("analise", analise);
        model.addAttribute("bancoAtual", ambienteService.getBancoAtual());
        model.addAttribute("bancoDescricao", ambienteService.getBancoDescricao());
        model.addAttribute("ocrAtual", ambienteService.getOcrAtual());
        return "analise-detalhe";
    }

    @PostMapping("/analises/{id}/resposta")
    public String registrarResposta(
            @PathVariable Long id,
            @RequestParam("respostaUsuario") String respostaUsuario,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        Usuario usuario = usuarioService.buscarUsuarioAutenticado(authentication);
        analiseService.registrarRespostaUsuario(id, usuario, respostaUsuario);
        redirectAttributes.addFlashAttribute("sucesso", "Resposta registrada.");
        return "redirect:/analises/" + id;
    }
}
