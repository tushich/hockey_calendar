import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public interface siteSPBHL {

    static List<Match> getMatchTable(String team_id) {

        String url = String.format("http://spbhl.ru/Schedule?TeamID=%s", team_id);
        // TODO Добавить выборку по сайтам. Добавить ФХСПб

        List<Match> matchTable = new ArrayList<>();

        Document doc;

        try {
            doc = Jsoup.connect(url).get();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.print("Ошибка чтения страницы HTML");
            return matchTable;
        }

        Elements rows = doc.getElementById("MatchGridView").select("tr");

        for (int i = 1; i < rows.size(); i++) {
            Match match = new Match();
            Elements cols = rows.get(i).select((i == 0) ? "th" : "td");// разбиваем полученную строку по тегу на столбы

            Elements matchElements = cols.get(6).select("a");

            if (matchElements.size() > 0) {
                // Вытащим matchID из ссылки. Состоит он из IDТурнира + v + IDМатча
                String matchID = matchElements.get(0).attributes().toString();

                // Добавим ссылку на матч
                match.setLinkMatch("https://spbhl.ru/" + matchID.replace(" href=\"", "")
                        .replace("\"", "")
                        .replace("&amp;", "&"));

                matchID = matchID.replace(" href=\"Match.aspx?TournamentID=", "");
                matchID = matchID.replace("MatchID=", "");
                matchID = matchID.replace("\"", "");
                matchID = matchID.replace("&amp;", "v");

                match.setMatchID(matchID);
            }

            match.setStartDateTime(cols.get(3).text(), cols.get(4).text());
            match.setProtokolExist((cols.get(8).select("a").size()) > 0);
            match.setTournament(cols.get(0).text());
            match.setRound(cols.get(1).text());
            match.setNumber(cols.get(2).text());
            match.setStadium(cols.get(5).text());
            match.setTeams(cols.get(6).text());
            match.setCount(cols.get(7).text());
            match.setTeam_id(team_id);

            matchTable.add(match);

        }

        return matchTable;
    }
}
