package com.nextup.tiers;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Map;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public final class TierCommand {
    private TierCommand() {}

    public static void register(CommandDispatcher<FabricClientCommandSource> d) {
        d.register(literal("tier")

                // ---- /tier mode [modalidade]
                .then(literal("mode")
                        .executes(ctx -> {
                            msg(ctx, "Modalidade atual: " + TierManager.getMode()
                                    + "  |  Opcoes: " + String.join(", ", TierManager.MODES));
                            return 1;
                        })
                        .then(argument("modo", StringArgumentType.word())
                                .suggests((c, b) -> CommandSource.suggestMatching(TierManager.MODES, b))
                                .executes(ctx -> {
                                    String m = TierManager.normalize(StringArgumentType.getString(ctx, "modo"));
                                    if (!TierManager.MODES.contains(m)) {
                                        msg(ctx, "Modalidade invalida. Opcoes: " + String.join(", ", TierManager.MODES));
                                        return 0;
                                    }
                                    TierManager.setMode(m);
                                    msg(ctx, "Agora mostrando: " + m);
                                    return 1;
                                })))

                // ---- /tier position front|back
                .then(literal("position")
                        .executes(ctx -> {
                            msg(ctx, "Posicao atual: " + ("back".equals(TierManager.getPosition()) ? "ATRAS do nome" : "NA FRENTE do nome")
                                    + "  |  Use /tier position front ou /tier position back");
                            return 1;
                        })
                        .then(literal("front").executes(ctx -> {
                            TierManager.setPosition("front");
                            msg(ctx, "Tier agora fica NA FRENTE do nome.");
                            return 1;
                        }))
                        .then(literal("back").executes(ctx -> {
                            TierManager.setPosition("back");
                            msg(ctx, "Tier agora fica ATRAS do nome.");
                            return 1;
                        })))

                // ---- /tier alias add <nickJogo> <nickSite> | remove <nickJogo> | list
                .then(literal("alias")
                        .then(literal("add")
                                .then(argument("jogo", StringArgumentType.word())
                                        .then(argument("site", StringArgumentType.greedyString())
                                                .executes(ctx -> {
                                                    String jogo = StringArgumentType.getString(ctx, "jogo");
                                                    String site = StringArgumentType.getString(ctx, "site").trim();
                                                    TierManager.addAlias(jogo, site);
                                                    msg(ctx, "Apelido salvo: " + jogo + " -> " + site);
                                                    return 1;
                                                }))))
                        .then(literal("remove")
                                .then(argument("jogo", StringArgumentType.word())
                                        .executes(ctx -> {
                                            String jogo = StringArgumentType.getString(ctx, "jogo");
                                            msg(ctx, TierManager.removeAlias(jogo)
                                                    ? "Apelido removido: " + jogo
                                                    : "Nao existe apelido pra " + jogo);
                                            return 1;
                                        })))
                        .then(literal("list").executes(ctx -> {
                            Map<String, String> a = TierManager.getAliases();
                            if (a.isEmpty()) {
                                msg(ctx, "Nenhum apelido. Use /tier alias add <nickJogo> <nickSite>");
                            } else {
                                a.forEach((k, v) -> msg(ctx, k + " -> " + v));
                            }
                            return 1;
                        })))

                // ---- /tier set <nick> <modo> <tier|pontos>
                .then(literal("set")
                        .then(argument("nick", StringArgumentType.word())
                                .then(argument("modo", StringArgumentType.word())
                                        .suggests((c, b) -> CommandSource.suggestMatching(TierManager.MODES, b))
                                        .then(argument("valor", StringArgumentType.word())
                                                .executes(ctx -> {
                                                    String nick = StringArgumentType.getString(ctx, "nick");
                                                    String modo = StringArgumentType.getString(ctx, "modo");
                                                    String valor = StringArgumentType.getString(ctx, "valor");
                                                    String erro = TierManager.setManual(nick, modo, valor);
                                                    if (erro != null) {
                                                        msg(ctx, erro);
                                                        return 0;
                                                    }
                                                    msg(ctx, "Salvo: " + nick + " / " + TierManager.normalize(modo) + " = " + valor.toUpperCase());
                                                    return 1;
                                                })))))

                // ---- /tier unset <nick> [modo]
                .then(literal("unset")
                        .then(argument("nick", StringArgumentType.word())
                                .executes(ctx -> {
                                    String nick = StringArgumentType.getString(ctx, "nick");
                                    msg(ctx, TierManager.removeManual(nick, null)
                                            ? "Tiers manuais de " + nick + " removidos."
                                            : "Nao tem tier manual pra " + nick);
                                    return 1;
                                })
                                .then(argument("modo", StringArgumentType.word())
                                        .suggests((c, b) -> CommandSource.suggestMatching(TierManager.MODES, b))
                                        .executes(ctx -> {
                                            String nick = StringArgumentType.getString(ctx, "nick");
                                            String modo = StringArgumentType.getString(ctx, "modo");
                                            msg(ctx, TierManager.removeManual(nick, modo)
                                                    ? "Removido: " + nick + " / " + TierManager.normalize(modo)
                                                    : "Nao tem tier manual desse jeito.");
                                            return 1;
                                        }))))

                // ---- /tier manual  (lista os tiers colocados na mao)
                .then(literal("manual").executes(ctx -> {
                    Map<String, Map<String, String>> m = TierManager.getManual();
                    if (m.isEmpty()) {
                        msg(ctx, "Nenhum tier manual. Use /tier set <nick> <modo> <tier>");
                    } else {
                        m.forEach((nick, modes) -> msg(ctx, nick + ": " + modes));
                    }
                    return 1;
                }))

                // ---- /tier next  (passa pra proxima modalidade)
                .then(literal("next").executes(ctx -> {
                    msg(ctx, "Agora mostrando: " + TierManager.nextMode());
                    return 1;
                }))

                // ---- /tier toggle  (liga/desliga tudo)
                .then(literal("toggle").executes(ctx -> {
                    TierManager.setEnabled(!TierManager.isEnabled());
                    msg(ctx, "Tiers " + (TierManager.isEnabled() ? "LIGADOS" : "DESLIGADOS"));
                    return 1;
                }))

                // ---- /tier show tab|nametag on|off
                .then(literal("show")
                        .executes(ctx -> {
                            msg(ctx, "TAB: " + onoff(TierManager.isShowTab())
                                    + "  |  Acima da cabeca: " + onoff(TierManager.isShowNametag())
                                    + "  |  Mudar: /tier show tab on|off  ou  /tier show nametag on|off");
                            return 1;
                        })
                        .then(literal("tab")
                                .then(literal("on").executes(ctx -> {
                                    TierManager.setShowTab(true);
                                    msg(ctx, "Tier no TAB: LIGADO");
                                    return 1;
                                }))
                                .then(literal("off").executes(ctx -> {
                                    TierManager.setShowTab(false);
                                    msg(ctx, "Tier no TAB: DESLIGADO");
                                    return 1;
                                })))
                        .then(literal("nametag")
                                .then(literal("on").executes(ctx -> {
                                    TierManager.setShowNametag(true);
                                    msg(ctx, "Tier acima da cabeca: LIGADO");
                                    return 1;
                                }))
                                .then(literal("off").executes(ctx -> {
                                    TierManager.setShowNametag(false);
                                    msg(ctx, "Tier acima da cabeca: DESLIGADO");
                                    return 1;
                                }))))

                // ---- /tier player <nick>  (mostra tudo sobre um jogador)
                .then(literal("player")
                        .then(argument("nick", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    String nick = StringArgumentType.getString(ctx, "nick").trim();
                                    List<String> lines = TierManager.describePlayer(nick);
                                    if (lines == null) {
                                        msg(ctx, "Nao achei " + nick + " no site.");
                                        return 0;
                                    }
                                    lines.forEach(l -> msg(ctx, l));
                                    return 1;
                                })))

                // ---- /tier top  (top 10 do Overall)
                .then(literal("top").executes(ctx -> {
                    List<String> t = TierManager.top(10);
                    if (t.isEmpty()) {
                        msg(ctx, "Sem dados ainda. Use /tier refresh");
                        return 0;
                    }
                    msg(ctx, "Top 10 Overall:");
                    t.forEach(l -> msg(ctx, l));
                    return 1;
                }))

                .then(literal("refresh").executes(ctx -> {
                    msg(ctx, "Atualizando...");
                    TierManager.refresh(s -> MinecraftClient.getInstance().execute(() -> msg(ctx, s)));
                    return 1;
                }))

                .then(literal("url")
                        .executes(ctx -> {
                            msg(ctx, "Link atual: " + TierManager.getUrl()
                                    + "  |  Trocar: /tier url <link>  |  Voltar ao padrao: /tier url reset");
                            return 1;
                        })
                        .then(argument("link", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    String link = StringArgumentType.getString(ctx, "link").trim();
                                    if (link.equalsIgnoreCase("reset")) link = TierManager.DEFAULT_URL;
                                    TierManager.setUrl(link);
                                    msg(ctx, "Link salvo: " + link + " - buscando os dados...");
                                    TierManager.refresh(s -> MinecraftClient.getInstance().execute(() -> msg(ctx, s)));
                                    return 1;
                                })))

                .then(literal("fuzzy")
                        .executes(ctx -> {
                            msg(ctx, "Nick parecido: " + (TierManager.isFuzzy() ? "LIGADO" : "DESLIGADO"));
                            return 1;
                        })
                        .then(literal("on").executes(ctx -> {
                            TierManager.setFuzzy(true);
                            msg(ctx, "Nick parecido LIGADO");
                            return 1;
                        }))
                        .then(literal("off").executes(ctx -> {
                            TierManager.setFuzzy(false);
                            msg(ctx, "Nick parecido DESLIGADO");
                            return 1;
                        })))

                .then(literal("version").executes(ctx -> {
                    msg(ctx, "NextUp Tiers v2.0 | comandos: mode, next, toggle, show, position, player, top, alias, set, unset, manual, refresh, url, fuzzy, status, debug");
                    return 1;
                }))
                .then(literal("debug").executes(ctx -> {
                    msg(ctx, TierManager.getStatus());
                    msg(ctx, "Detalhes salvos em: " + TierManager.getDebugPath());
                    return 1;
                }))

                .then(literal("status").executes(ctx -> {
                    msg(ctx, TierManager.getStatus() + " (modalidade: " + TierManager.getMode()
                            + ", posicao: " + TierManager.getPosition() + ")");
                    return 1;
                })));
    }

    private static String onoff(boolean b) { return b ? "LIGADO" : "DESLIGADO"; }

    private static void msg(CommandContext<FabricClientCommandSource> ctx, String s) {
        ctx.getSource().sendFeedback(Text.literal("[NextUp Tiers] " + s));
    }
}
