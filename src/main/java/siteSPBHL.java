import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public interface siteSPBHL {

    static List<Match> getMatchTable(String team_id, String siteID) {

        int startDateCol = 2,
                startTimeCol = 3,
                protokolExistCol = 7,
                tournamentCol = 0,
                roundCol = 99,
                numberCol = 1,
                stadiumCol = 4,
                teamsCol = 5,
                countCol = 6;

        if(siteID.equals("fhspb.ru"))
        {
            startDateCol = 2;
            startTimeCol = 3;
            protokolExistCol = 7;
            tournamentCol = 0;
            roundCol = 99;
            numberCol = 1;
            stadiumCol = 4;
            teamsCol = 5;
            countCol = 6;

        }
        else //if(siteID.equals("spbhl.ru"))
        {
            startDateCol = 3;
            startTimeCol = 4;
            protokolExistCol = 8;
            tournamentCol = 0;
            roundCol = 1;
            numberCol = 2;
            stadiumCol = 5;
            teamsCol = 6;
            countCol = 7;
        }


        String url = String.format("https://" + siteID + "/Schedule?TeamID=%s", team_id);
        // TODO Добавить выборку по сайтам. Добавить ФХСПб

        List<Match> matchTable = new ArrayList<>();

        Document doc;

        try {
            doc = Jsoup.connect(url).get();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.print("Ошибка чтения страницы HTML"); // TODO Отправлять ошибку в телегу админу
            return matchTable;
        }

        Elements rows = doc.getElementById("MatchGridView").select("tr");

        for (int i = 1; i < rows.size(); i++) {
            Match match = new Match();
            Elements cols = rows.get(i).select((i == 0) ? "th" : "td");// разбиваем полученную строку по тегу на столбы

            Elements matchElements = cols.get(teamsCol).select("a");

            if (matchElements.size() > 0) {
                // Вытащим matchID из ссылки. Состоит он из IDТурнира + v + IDМатча
                String matchID = matchElements.get(tournamentCol).attributes().toString();

                // Добавим ссылку на матч
                match.setLinkMatch("https://spbhl.ru/" + matchID.replace(" href=\"", "")
                        .replace("\"", "")
                        .replace("&amp;", "&"));

                matchID = matchID.replace(" href=\"Match.aspx?TournamentID=", "");
                matchID = matchID.replace(" href=\"Match?TournamentID=", "");
                matchID = matchID.replace("MatchID=", "");
                matchID = matchID.replace("\"", "");
                matchID = matchID.replace("&amp;", "v");

                match.setMatchID(matchID);
            }

            String time = cols.get(startTimeCol).text();
            time = time.replace("--:--","00:00");

            match.setStartDateTime(cols.get(startDateCol).text(), time);
            match.setProtokolExist((cols.get(protokolExistCol).select("a").size()) > 0);
            match.setTournament(cols.get(tournamentCol).text());
            // Не используем
            // match.setRound(cols.get(roundCol).text());
            match.setNumber(cols.get(numberCol).text());
            match.setStadium(cols.get(stadiumCol).select("a[href]").attr("title"));
            match.setTeams(cols.get(teamsCol).text());
            match.setCount(cols.get(countCol).text());
            match.setTeam_id(team_id);
            match.setSiteID(siteID);

            matchTable.add(match);

        }

        return matchTable;
    }
}
