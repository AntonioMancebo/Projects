
import telebot
bot = telebot.TeleBot('5809880380:AAGaaoPSTOiHqhTbBHeGSBB_IvmdBMoZh8w')

@bot.message_handler(commands=['start', 'ayuda', 'inicio'])
def send_welcome(message):
    bot.reply_to(message, "¿En que puedo ayudarte?")


