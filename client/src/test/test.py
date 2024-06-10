import smtplib
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart
import os


def main():
    send_email_notification(
        subject="Script Execution Complete",
        body="The script has finished running.",
        to_email="joaquingirod@gmail.com"
    )

def send_email_notification(subject, body, to_email):
    from_email = "joaquingirodnotifier@gmail.com"
    from_password = "vtre ncgw lddl drkd "

    msg = MIMEMultipart()
    msg['From'] = from_email
    msg['To'] = to_email
    msg['Subject'] = subject

    msg.attach(MIMEText(body, 'plain'))

    try:
        server = smtplib.SMTP('smtp.gmail.com', 587)
        server.starttls()
        server.login(from_email, from_password)
        text = msg.as_string()
        server.sendmail(from_email, to_email, text)
        server.quit()
        print("Email sent successfully!")
    except Exception as e:
        print(f"Failed to send email: {e}")

if __name__ == "__main__":
    main()