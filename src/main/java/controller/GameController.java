package controller;
import org.springframework.ui.Model;
import Model.Player;
//import ch.qos.logback.core.model.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class GameController {
    private Player player1;
    private Player player2;
    private Player currentPlayer;

    @GetMapping("/board")
    public String displayBoard(Model model)
    {
        if(currentPlayer != null)
        {
            char[][] board = currentPlayer.getBoard();
            model.addAttribute("board", board);
            return "board-view";
        }
        else
        {
            model.addAttribute("message", "Игра еще не начата");
            return "message-view";
        }
    }
    @PostMapping("/start")
    public String startGame(String playerName1, String playerName2)
    {
        player1 = new Player(playerName1);
        player2 = new Player(playerName2);
        currentPlayer = player1;
        return "redirect:/board";
    }

}
