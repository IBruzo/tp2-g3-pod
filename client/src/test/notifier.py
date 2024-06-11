import smtplib
import argparse
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart

def send_email_notification(subject, body, to_email):
    from_email = "joaquingirodnotifier@gmail.com"
    from_password = "vtre ncgw lddl drkd"

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

def parse_args():
    parser = argparse.ArgumentParser(description='Send Mail to Someone using joaquingirodnotifier@gmail.com')
    parser.add_argument('--subject', type=str, required=True, help='Mail Subject')
    parser.add_argument('--body', type=str, required=True, help='Mail Body')
    parser.add_argument('--to', type=str, required=True, help='Mail to...')
    return parser.parse_args()

def main():
    args = parse_args()
    send_email_notification(
        args.subject,
        args.body,
        args.to
    )


if __name__ == "__main__":
    main()