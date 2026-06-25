package com.trabalho.verificadocs.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.trabalho.verificadocs.model.Analise;
import com.trabalho.verificadocs.model.StatusAnalise;
import com.trabalho.verificadocs.model.TipoDocumento;
import com.trabalho.verificadocs.model.Usuario;
import com.trabalho.verificadocs.service.AmbienteService;
import com.trabalho.verificadocs.service.AnaliseService;
import com.trabalho.verificadocs.service.BancoDiagnosticoService;
import com.trabalho.verificadocs.service.UsuarioService;

@Controller
public class DocumentoController {

    private final AnaliseService analiseService;
    private final UsuarioService usuarioService;
    private final AmbienteService ambienteService;
    private final BancoDiagnosticoService bancoDiagnosticoService;

    public DocumentoController(
            AnaliseService analiseService,
            UsuarioService usuarioService,
            AmbienteService ambienteService,
            BancoDiagnosticoService bancoDiagnosticoService) {
        this.analiseService = analiseService;
        this.usuarioService = usuarioService;
        this.ambienteService = ambienteService;
        this.bancoDiagnosticoService = bancoDiagnosticoService;
    }

    @GetMapping("/documentos")
    public String documentos(Authentication authentication, Model model) {
        Usuario usuario = usuarioService.buscarUsuarioAutenticado(authentication);
        List<Analise> analises = analiseService.listarAnalises(usuario);

        model.addAttribute("usuario", usuario);
        model.addAttribute("analises", analises);
        model.addAttribute("tiposDocumento", TipoDocumento.values());
        model.addAttribute("totalAnalises", analises.size());
        model.addAttribute("totalConcluidas", analises.stream()
                .filter(analise -> analise.getStatus() == StatusAnalise.CONCLUIDA)
                .count());
        model.addAttribute("totalSuspeitas", analises.stream()
                .filter(Analise::isIndicioEdicao)
                .count());
        model.addAttribute("bancoAtual", ambienteService.getBancoAtual());
        model.addAttribute("bancoDescricao", ambienteService.getBancoDescricao());
        model.addAttribute("ocrAtual", ambienteService.getOcrAtual());
        model.addAttribute("bancoResumo", bancoDiagnosticoService.gerarResumo());
        return "documentos";
    }

    @PostMapping("/documentos")
    public String enviarDocumento(
            Authentication authentication,
            @RequestParam("arquivo") MultipartFile arquivo,
            @RequestParam("tipoDocumento") TipoDocumento tipoDocumento,
            RedirectAttributes redirectAttributes) {
        Usuario usuario = usuarioService.buscarUsuarioAutenticado(authentication);

        try {
            Analise analise = analiseService.analisarDocumento(usuario, arquivo, tipoDocumento);
            redirectAttributes.addFlashAttribute("sucesso", "Documento analisado com sucesso.");
            return "redirect:/analises/" + analise.getId();
        } catch (IllegalArgumentException | IOException ex) {
            redirectAttributes.addFlashAttribute("erro", ex.getMessage());
            return "redirect:/documentos";
        }
    }
}
