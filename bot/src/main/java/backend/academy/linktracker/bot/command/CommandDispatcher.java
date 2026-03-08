package backend.academy.linktracker.bot.command;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.model.AddLinkRequest;
import backend.academy.linktracker.bot.model.LinkResponse;
import backend.academy.linktracker.bot.model.ListLinksResponse;
import backend.academy.linktracker.bot.service.state.ChatState;
import backend.academy.linktracker.bot.service.state.TrackState;
import com.pengrad.telegrambot.request.SendMessage;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

@Component
public class CommandDispatcher {

    private static final String HELP_TEXT = """
        /start — начать работу
        /help — список доступных команд
        /track — добавить ссылку в отслеживание
        /untrack — удалить ссылку из отслеживания
        /list [tag] — показать отслеживаемые ссылки
        /cancel — отменить текущий диалог
        """;

    private final ScrapperClient scrapperClient;
    private final Map<Long, ChatState> states = new ConcurrentHashMap<>();

    public CommandDispatcher(ScrapperClient scrapperClient) {
        this.scrapperClient = scrapperClient;
    }

    public SendMessage dispatch(long chatId, String messageText) {
        ChatState state = states.getOrDefault(chatId, ChatState.idle());
        String normalizedText = messageText == null ? "" : messageText.trim();

        if (state.state() != TrackState.IDLE) {
            return handleDialog(chatId, state, normalizedText);
        }

        return handleCommand(chatId, normalizedText);
    }

    private SendMessage handleDialog(long chatId, ChatState state, String text) {
        BotCommand command = BotCommand.fromText(CommandParser.extract(text));

        if (command == BotCommand.CANCEL) {
            states.put(chatId, ChatState.idle());
            return new SendMessage(chatId, "Диалог отменён.");
        }

        if (command != BotCommand.UNKNOWN) {
            states.put(chatId, ChatState.idle());
            return handleCommand(chatId, text);
        }

        return switch (state.state()) {
            case WAITING_TRACK_LINK -> handleTrackLink(chatId, text);
            case WAITING_TRACK_TAGS -> handleTrackTags(chatId, state.pendingLink(), text);
            case WAITING_UNTRACK_LINK -> handleUntrackLink(chatId, text);
            case IDLE -> handleCommand(chatId, text);
        };
    }

    private SendMessage handleCommand(long chatId, String text) {
        BotCommand command = BotCommand.fromText(CommandParser.extract(text));
        List<String> args = CommandParser.arguments(text);

        return switch (command) {
            case START -> {
                try {
                    scrapperClient.registerChat(chatId);
                } catch (RuntimeException exception) {
                    // Не блокируем /start из-за недоступности scrapper
                }
                yield new SendMessage(
                        chatId, "Добро пожаловать! Используйте /help, чтобы посмотреть доступные команды.");
            }
            case HELP -> new SendMessage(chatId, HELP_TEXT);
            case TRACK -> {
                states.put(chatId, new ChatState(TrackState.WAITING_TRACK_LINK, null));
                yield new SendMessage(chatId, "Отправьте ссылку для отслеживания.");
            }
            case UNTRACK -> {
                states.put(chatId, new ChatState(TrackState.WAITING_UNTRACK_LINK, null));
                yield new SendMessage(chatId, "Отправьте ссылку, которую нужно перестать отслеживать.");
            }
            case LIST -> formatList(chatId, args.isEmpty() ? null : args.getFirst());
            case CANCEL -> new SendMessage(chatId, "Сейчас нечего отменять.");
            case UNKNOWN ->
                new SendMessage(
                        chatId, "Неизвестная команда. Воспользуйтесь /help, чтобы посмотреть список доступных команд.");
        };
    }

    private SendMessage handleTrackLink(long chatId, String text) {
        URI link = parseTrackedUri(text);
        if (link == null) {
            return new SendMessage(chatId, "Некорректная ссылка. Введите корректный URL (http/https).");
        }

        states.put(chatId, new ChatState(TrackState.WAITING_TRACK_TAGS, link));
        return new SendMessage(chatId, "Введите теги через запятую (необязательно). Можно отправить пустое сообщение.");
    }

    private SendMessage handleTrackTags(long chatId, URI link, String text) {
        List<String> tags = Arrays.stream(text.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();

        try {
            scrapperClient.addLink(chatId, new AddLinkRequest(link, tags, List.of()));
            states.put(chatId, ChatState.idle());
            return new SendMessage(chatId, "Ссылка добавлена в отслеживание.");
        } catch (HttpClientErrorException.Conflict exception) {
            states.put(chatId, ChatState.idle());
            return new SendMessage(chatId, "Ссылка уже отслеживается");
        } catch (HttpClientErrorException.NotFound exception) {
            states.put(chatId, ChatState.idle());
            return new SendMessage(chatId, "Чат не зарегистрирован. Отправьте /start и повторите попытку.");
        } catch (RuntimeException exception) {
            return new SendMessage(
                    chatId,
                    "Не удалось сохранить ссылку: Scrapper недоступен. Повторите ввод тегов или отправьте /cancel.");
        }
    }

    private SendMessage handleUntrackLink(long chatId, String text) {
        URI link = parseTrackedUri(text);
        if (link == null) {
            return new SendMessage(chatId, "Некорректная ссылка. Введите корректный URL (http/https).");
        }

        try {
            LinkResponse response = scrapperClient.removeLink(chatId, link);
            states.put(chatId, ChatState.idle());
            return new SendMessage(chatId, "Ссылка удалена из отслеживания: " + response.url());
        } catch (HttpClientErrorException exception) {
            states.put(chatId, ChatState.idle());
            return new SendMessage(chatId, "Ссылка не найдена в отслеживаемых.");
        } catch (RuntimeException exception) {
            return new SendMessage(
                    chatId,
                    "Не удалось удалить ссылку: Scrapper недоступен. Повторите ввод ссылки или отправьте /cancel.");
        }
    }

    private SendMessage formatList(long chatId, String tagFilter) {
        List<LinkResponse> links;
        try {
            ListLinksResponse response = scrapperClient.getLinks(chatId);
            links = response.links();
        } catch (RuntimeException exception) {
            return new SendMessage(chatId, "Не удалось получить список ссылок: Scrapper недоступен.");
        }

        if (tagFilter != null && !tagFilter.isBlank()) {
            links = links.stream()
                    .filter(link -> link.tags() != null && link.tags().stream().anyMatch(tagFilter::equalsIgnoreCase))
                    .toList();
        }

        if (links.isEmpty()) {
            return new SendMessage(chatId, "Список отслеживаемых ссылок пуст.");
        }

        String body = links.stream()
                .map(link -> "• " + link.url() + formatTags(link.tags()))
                .reduce((a, b) -> a + "\n" + b)
                .orElse("Список отслеживаемых ссылок пуст.");

        return new SendMessage(chatId, body);
    }

    private String formatTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return "";
        }
        return " [" + String.join(", ", tags) + "]";
    }

    private URI parseTrackedUri(String raw) {
        try {
            URI uri = URI.create(raw.trim());
            String scheme = uri.getScheme();
            if (scheme == null || uri.getHost() == null) {
                return null;
            }
            if (!scheme.equals("http") && !scheme.equals("https")) {
                return null;
            }
            return uri;
        } catch (RuntimeException exception) {
            return null;
        }
    }
}
