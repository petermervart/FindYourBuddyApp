import base64
from django.views.decorators.csrf import csrf_exempt
from django.core.serializers.json import DjangoJSONEncoder
from django.http import HttpResponse
from django.db.models import Q
import json
import re
from . import models
from django.contrib.auth.hashers import make_password, check_password
from urllib.parse import urlparse
from urllib.parse import parse_qs
from django.utils import timezone
import imghdr

def get_password_name(request):
    auth_header = request.META['HTTP_AUTHORIZATION']
    encoded_credentials = auth_header.split(' ')[1]
    decoded_credentials = base64.b64decode(encoded_credentials).decode("utf-8").split(':')
    username = decoded_credentials[0]
    password = decoded_credentials[1]
    return username, password

@csrf_exempt
def users_no_id(request):
    if request.method == "GET":
        url = str(request.build_absolute_uri())
        parsed_url = urlparse(url)
        params = parse_qs(parsed_url.query)
        errors = []
        user_id = 0
        record = {}
        if "user_id" in params.keys():
            for key in params.keys():
                if key == "user_id":
                    try:
                        user_id = int(params["user_id"][0])
                    except ValueError:
                        errors.append("user id not integer")
            if not errors:
                if models.Users.objects.filter(iduser=user_id).exists():
                    result = models.Users.objects.filter(iduser=user_id)[0]
                    if result.picture is not None:
                        try:
                            file = open(result.picture, "rb")
                            picture_byte = str(base64.b64encode(file.read()))
                            picture_byte = picture_byte[2: len(picture_byte) - 2]
                            record["picture"] = picture_byte
                        except Exception:
                            record["picture"] = None
                    else:
                        record["picture"] = None
                    record["description"] = result.description
                    record["name"] = result.name
                    result = json.dumps({"user": record}, cls=DjangoJSONEncoder)
                    response = HttpResponse(result, content_type='application/json')
                    response.status_code = 200
                else:
                    response = HttpResponse(json.dumps({"error": "This user does not exists"}),
                                            content_type='application/json')
                    response.status_code = 404
            else:
                result = json.dumps({"errors": errors}, cls=DjangoJSONEncoder)
                response = HttpResponse(result, content_type='application/json')
                response.status_code = 400
        else:
            result = json.dumps({"errors": errors}, cls=DjangoJSONEncoder)
            response = HttpResponse(result, content_type='application/json')
            response.status_code = 400

        return response


@csrf_exempt
def users(request, id):
    if request.method == "PUT":
        if not request.body:
            parameters = {}
        else:
            parameters = json.loads(request.body)
        if('HTTP_AUTHORIZATION' in request.META):
            name_real, password = get_password_name(request)
            user = models.Users.objects.filter(name=name_real).first()
            if user and user.iduser == id and check_password(password, user.password):
                errors = []
                if models.Users.objects.filter(iduser=id).exists() and "user" in parameters:
                    res = models.Users.objects.filter(iduser=id)
                    res = res[0]
                    for key in parameters["user"].keys():
                        if str(key) == "name":
                            if 35 >= len(parameters["user"]["name"]) >= 4:
                                res.name = parameters["user"]["name"]
                            else:
                                errors.append({"field": "name", "reasons": "too long or too short name (35 >= name >= 4)"})
                        if str(key) == "description":
                            if 500 >= len(parameters["user"]["description"]):
                                res.description = parameters["user"]["description"]
                            else:
                                errors.append({"field": "description", "reasons": "too long description"})
                        if str(key) == "picture":
                            try:
                                image = parameters["user"]["picture"]
                                image_dec = base64.b64decode(image)
                                extension = imghdr.what(None, h=image_dec)
                                path = "pictures/" + str(id) + "_picture." + extension
                                file = open(path, "wb")
                                file.write(image_dec)
                                file.close()
                                res.picture = path
                            except Exception:
                                errors.append({"field": "picture", "reasons": "bad picture"})

                    if errors:
                        result = json.dumps({"errors": errors}, cls=DjangoJSONEncoder)
                        response = HttpResponse(result, content_type='application/json')
                        response.status_code = 400
                    else:
                        res.save()
                        response = HttpResponse(content_type='application/json')
                        response.status_code = 204
                else:
                    if "user" not in parameters:
                        response = HttpResponse(content_type='application/json')
                        response.status_code = 422
                    else:
                        response = HttpResponse(json.dumps({"error": {"message": "Not found"}}),
                                                content_type='application/json')
                        response.status_code = 404
            else:
                response = HttpResponse(json.dumps({"error": {"login": "Bad login"}}),
                                        content_type='application/json')
                response.status_code = 400

        else:
            response = HttpResponse(json.dumps({"request": {"login": "Bad request"}}),
                                    content_type='application/json')
            response.status_code = 400

        return response


@csrf_exempt
def messages(request):
    if request.method == "GET":
        url = str(request.build_absolute_uri())
        parsed_url = urlparse(url)
        params = parse_qs(parsed_url.query)
        errors = []
        records = []
        showed = {}
        user_id = 0
        user_1 = 0
        user_2 = 0
        if (len(params.keys()) == 1) and "user_id" in params.keys():
            for key in params.keys():
                if key == "user_id":
                    try:
                        user_id = int(params["user_id"][0])
                    except ValueError:
                        errors.append("user id not integer")

            if 'HTTP_AUTHORIZATION' in request.META:
                name_real, password = get_password_name(request)
                user = models.Users.objects.filter(name=name_real).first()
                if user and user.iduser == user_id and check_password(password, user.password):
                    if not errors:
                        if models.Users.objects.filter(iduser=user_id).exists():
                            result = models.Messages.objects.filter(Q(sender_id=user_id) | Q(reciever_id=user_id)).distinct(
                                "sender_id", "reciever_id").order_by("sender_id", "reciever_id", "-created_at").values_list(
                                'idmessages', flat=True)
                            result = models.Messages.objects.filter(idmessages__in=result).order_by("-created_at")
                            for record in result:
                                if str(record.sender_id) not in showed and str(record.reciever_id) not in showed:
                                    if record.sender_id == user_id:
                                        showed[str(record.reciever_id)] = 1
                                    else:
                                        showed[str(record.sender_id)] = 1
                                    sender = models.Users.objects.filter(iduser=record.sender_id)[0]
                                    reciever = models.Users.objects.filter(iduser=record.reciever_id)[0]
                                    records.append(
                                        {"sender_id": sender.iduser, "sender_name": sender.name, "reciever_id": reciever.iduser,
                                         "reciever_name": reciever.name, "created_at": record.created_at})
                            result = json.dumps({"results": records}, cls=DjangoJSONEncoder)
                            response = HttpResponse(result, content_type='application/json')
                            response.status_code = 200
                        else:
                            response = HttpResponse(json.dumps({"error": "This user does not exists"}),
                                                    content_type='application/json')
                            response.status_code = 404
                    else:
                        result = json.dumps({"errors": errors}, cls=DjangoJSONEncoder)
                        response = HttpResponse(result, content_type='application/json')
                        response.status_code = 400
                else:
                    response = HttpResponse(json.dumps({"error": {"login": "Bad login"}}),
                                            content_type='application/json')
                    response.status_code = 400

            else:
                response = HttpResponse(json.dumps({"request": {"login": "Bad request"}}), content_type='application/json')
                response.status_code = 400


        elif (len(params.keys()) == 2) and "user_1" in params.keys() and "user_2" in params.keys():
            for key in params.keys():
                if key == "user_1":
                    try:
                        user_1 = int(params["user_1"][0])
                    except ValueError:
                        errors.append("user_1 id not integer")
                if key == "user_2":
                    try:
                        user_2 = int(params["user_2"][0])
                    except ValueError:
                        errors.append("user_2 id not integer")

            if 'HTTP_AUTHORIZATION' in request.META:
                name_real, password = get_password_name(request)
                user = models.Users.objects.filter(name=name_real).first()
                if user and (user.iduser == user_1 or user.iduser == user_2) and check_password(password, user.password):
                    if not errors:
                        if models.Users.objects.filter(iduser=user_1).exists() and models.Users.objects.filter(
                                iduser=user_2).exists():
                            result = models.Messages.objects.filter((Q(sender_id=user_1) & Q(reciever_id=user_2)) | (
                                    Q(sender_id=user_2) & Q(reciever_id=user_1))).order_by("created_at")
                            for record in result:
                                sender = models.Users.objects.filter(iduser=record.sender_id)[0]
                                reciever = models.Users.objects.filter(iduser=record.reciever_id)[0]
                                records.append(
                                    {"sender_id": sender.iduser, "sender_name": sender.name, "reciever_id": reciever.iduser,
                                     "reciever_name": reciever.name, "text": record.text, "created_at": record.created_at})
                            result = json.dumps({"results": records}, cls=DjangoJSONEncoder)
                            response = HttpResponse(result, content_type='application/json')
                            response.status_code = 200
                        else:
                            response = HttpResponse(json.dumps({"error": "This user does not exists"}),
                                                    content_type='application/json')
                            response.status_code = 404
                    else:
                        errors.append("bad parameters")
                        result = json.dumps({"errors": errors}, cls=DjangoJSONEncoder)
                        response = HttpResponse(result, content_type='application/json')
                        response.status_code = 400
                else:
                    response = HttpResponse(json.dumps({"error": {"login": "Bad login"}}),
                                            content_type='application/json')
                    response.status_code = 400

            else:
                response = HttpResponse(json.dumps({"error": {"request": "Bad request"}}),
                                        content_type='application/json')
                response.status_code = 400
        else:
            response = HttpResponse(json.dumps({"error": {"request": "Bad request"}}),
                                    content_type='application/json')
            response.status_code = 400

        return response

    elif request.method == "POST":
        if not request.body:
            parameters = {}
        else:
            parameters = json.loads(request.body)
        errors = []
        text = ""
        sender_id = 0
        reciever_id = 0
        if "message" not in parameters or "sender_id" not in parameters["message"] or "reciever_id" not in parameters[
            "message"] or "text" not in parameters["message"]:
            errors.append({"field": "request", "reasons": "missing values"})
        else:
            for key in parameters["message"].keys():
                if str(key) == "reciever_id":
                    try:
                        reciever_id = int(parameters["message"]["reciever_id"])
                    except ValueError:
                        errors.append({"field": "reciever_id", "reasons": "bad id"})

                if str(key) == "sender_id":
                    try:
                        sender_id = int(parameters["message"]["sender_id"])
                    except ValueError:
                        errors.append({"field": "sender_id", "reasons": "bad id"})

                if str(key) == "text":
                    text = parameters["message"]["text"]
                    if len(text) <= 0 or len(text) > 350:
                        errors.append({"field": "text", "reasons": "empty or too long message"})

        if 'HTTP_AUTHORIZATION' in request.META and sender_id!=reciever_id and not errors:
            name_real, password = get_password_name(request)
            user = models.Users.objects.filter(name=name_real).first()
            if user and (user.iduser == sender_id or user.iduser == reciever_id) and check_password(password, user.password):
                if not errors and not models.Users.objects.filter(
                        iduser=reciever_id).exists() or not models.Users.objects.filter(iduser=sender_id).exists():
                    errors.append({"field": "reciever_id/user_id", "reasons": "not a valid user id"})

                if not models.Friends.objects.filter(
                        (Q(user_2=sender_id) & Q(user_1=reciever_id)) | (Q(user_2=reciever_id) & Q(user_1=sender_id))).exists():
                    errors.append({"field": "users", "reasons": "users are not friends"})

                if not errors:
                    current_time = timezone.now()
                    message = models.Messages(sender_id=sender_id, reciever_id=reciever_id, text=text, created_at=current_time)
                    message.save()
                    response = HttpResponse(content_type='application/json')
                    response.status_code = 201
                else:
                    result = json.dumps({"errors": errors}, cls=DjangoJSONEncoder)
                    response = HttpResponse(result, content_type='application/json')
                    response.status_code = 400
            else:
                response = HttpResponse(json.dumps({"error": {"login": "Bad login"}}),
                                        content_type='application/json')
                response.status_code = 400
        else:
            response = HttpResponse(json.dumps({"error": {"request": "Bad request"}}),
                                    content_type='application/json')
            response.status_code = 400

        return response


@csrf_exempt
def friends(request):
    if request.method == "GET":
        url = str(request.build_absolute_uri())
        parsed_url = urlparse(url)
        params = parse_qs(parsed_url.query)
        errors = []
        records = []
        user_id = 0
        if "user_id" in params.keys():
            for key in params.keys():
                if key == "user_id":
                    try:
                        user_id = int(params["user_id"][0])
                    except ValueError:
                        errors.append("user id not integer")
        else:
            errors.append("bad parameters")
            result = json.dumps({"errors": errors}, cls=DjangoJSONEncoder)
            response = HttpResponse(result, content_type='application/json')
            response.status_code = 400

        if 'HTTP_AUTHORIZATION' in request.META and not errors:
            name_real, password = get_password_name(request)
            user = models.Users.objects.filter(name=name_real).first()
            if user and (user.iduser == user_id) and check_password(password, user.password):
                if models.Users.objects.filter(iduser=user_id).exists():
                    result = models.Friends.objects.filter(Q(user_1=user_id) | Q(user_2=user_id)).order_by("-created_at")
                    for record in result:
                        user_1 = models.Users.objects.filter(iduser=record.user_1_id)[0]
                        user_2 = models.Users.objects.filter(iduser=record.user_2_id)[0]
                        if user_1.iduser == user_id:
                            records.append({"id": user_2.iduser, "name": user_2.name})
                        else:
                            records.append({"id": user_1.iduser, "name": user_1.name})
                    result = json.dumps({"results": records}, cls=DjangoJSONEncoder)
                    response = HttpResponse(result, content_type='application/json')
                    response.status_code = 200
                else:
                    response = HttpResponse(json.dumps({"error": "This user does not exists"}),
                                            content_type='application/json')
                    response.status_code = 404
            else:
                response = HttpResponse(json.dumps({"error": {"login": "Bad login"}}),
                                        content_type='application/json')
                response.status_code = 400
        else:
            response = HttpResponse(json.dumps({"error": {"request": "Bad request"}}), content_type='application/json')
            response.status_code = 400

        return response

    elif request.method == "POST":
        if not request.body:
            parameters = {}
        else:
            parameters = json.loads(request.body)
        errors = []
        user_1 = 0
        user_2 = 0
        if "friends" not in parameters or "user_1" not in parameters["friends"] or "user_2" not in parameters["friends"]:
            errors.append({"field": "request", "reasons": "missing values"})
        else:
            for key in parameters["friends"].keys():
                if key == "user_1":
                    try:
                        user_1 = int(parameters["friends"]["user_1"])
                    except ValueError:
                        errors.append({"field": "user_1", "reasons": "bad id"})

                if key == "user_2":
                    try:
                        user_2 = int(parameters["friends"]["user_2"])
                    except ValueError:
                        errors.append({"field": "user_2", "reasons": "bad id"})

        if 'HTTP_AUTHORIZATION' in request.META and user_1!=user_2 and not errors:
            name_real, password = get_password_name(request)
            user = models.Users.objects.filter(name=name_real).first()
            if user and (user.iduser == user_1 or user.iduser == user_2) and check_password(password, user.password):
                if not errors and not models.Users.objects.filter(iduser=user_1).exists() or not models.Users.objects.filter(
                        iduser=user_2).exists():
                    errors.append({"field": "user_1/user_2", "reasons": "not a valid user id"})

                if models.Friends.objects.filter(
                        (Q(user_2=user_2) & Q(user_1=user_1)) | (Q(user_2=user_1) & Q(user_1=user_2))).exists():
                    errors.append({"field": "user_1/user_2", "reasons": "friendship already exists"})

                if not errors:
                    current_time = timezone.now()
                    friendship = models.Friends(user_1_id=user_1, user_2_id=user_2, created_at=current_time)
                    friendship.save()
                    response = HttpResponse(content_type='application/json')
                    response.status_code = 201
                else:
                    result = json.dumps({"errors": errors}, cls=DjangoJSONEncoder)
                    response = HttpResponse(result, content_type='application/json')
                    response.status_code = 400
            else:
                response = HttpResponse(json.dumps({"error": {"login": "Bad login"}}),
                                        content_type='application/json')
                response.status_code = 400
        else:
            response = HttpResponse(json.dumps({"error": {"request": "Bad request"}}), content_type='application/json')
            response.status_code = 400

        return response

    if request.method == "DELETE":
        url = str(request.build_absolute_uri())
        parsed_url = urlparse(url)
        params = parse_qs(parsed_url.query)
        errors = []
        user_1 = 0
        user_2 = 0
        if "user_1" in params.keys() and "user_2" in params.keys():
            for key in params.keys():
                if key == "user_1":
                    try:
                        user_1 = int(params["user_1"][0])
                    except ValueError:
                        errors.append("user_1 id not integer")
                if key == "user_2":
                    try:
                        user_2 = int(params["user_2"][0])
                    except ValueError:
                        errors.append("user_2 id not integer")
        else:
            errors.append("bad parameters")
            result = json.dumps({"errors": errors}, cls=DjangoJSONEncoder)
            response = HttpResponse(result, content_type='application/json')
            response.status_code = 400

        if 'HTTP_AUTHORIZATION' in request.META and not errors:
            name_real, password = get_password_name(request)
            user = models.Users.objects.filter(name=name_real).first()
            if user and (user.iduser == user_1 or user.iduser == user_2) and check_password(password, user.password):
                if models.Friends.objects.filter(
                        (Q(user_2=user_2) & Q(user_1=user_1)) | (Q(user_2=user_1) & Q(user_1=user_2))).exists():
                    models.Friends.objects.filter(
                        (Q(user_2=user_2) & Q(user_1=user_1)) | (Q(user_2=user_1) & Q(user_1=user_2))).delete()
                    response = HttpResponse(content_type='application/json')
                    response.status_code = 204
                else:
                    response = HttpResponse(json.dumps({"error": "This friendship does not exists"}),
                                            content_type='application/json')
                    response.status_code = 404
            else:
                response = HttpResponse(json.dumps({"error": {"login": "Bad login"}}),
                                        content_type='application/json')
                response.status_code = 400
        else:
            response = HttpResponse(json.dumps({"error": {"request": "Bad request"}}), content_type='application/json')
            response.status_code = 400


        return response


@csrf_exempt
def friend_requests(request):
    if request.method == "GET":
        url = str(request.build_absolute_uri())
        parsed_url = urlparse(url)
        params = parse_qs(parsed_url.query)
        errors = []
        records = []
        user_id = 0
        if "user_id" in params.keys():
            for key in params.keys():
                if key == "user_id":
                    try:
                        user_id = int(params["user_id"][0])
                    except ValueError:
                        errors.append("user id not integer")
        else:
            errors.append("bad parameters")
            result = json.dumps({"errors": errors}, cls=DjangoJSONEncoder)
            response = HttpResponse(result, content_type='application/json')
            response.status_code = 400

        if 'HTTP_AUTHORIZATION' in request.META and not errors:
            name_real, password = get_password_name(request)
            user = models.Users.objects.filter(name=name_real).first()
            if user and (user.iduser == user_id) and check_password(password, user.password):
                if models.Users.objects.filter(iduser=user_id).exists():
                    result = models.Friend_requests.objects.filter(Q(sender_id=user_id) | Q(reciever_id=user_id)).order_by(
                        "-created_at")
                    for record in result:
                        sender = models.Users.objects.filter(iduser=record.sender_id)[0]
                        reciever = models.Users.objects.filter(iduser=record.reciever_id)[0]
                        records.append(
                            {"id": record.idfriend_requests, "sender_id": sender.iduser, "sender_name": sender.name,
                             "reciever_id": reciever.iduser, "reciever_name": reciever.name, "created_at": record.created_at})
                    result = json.dumps({"results": records}, cls=DjangoJSONEncoder)
                    response = HttpResponse(result, content_type='application/json')
                    response.status_code = 200
                else:
                    response = HttpResponse(json.dumps({"error": "This user does not exists"}),
                                            content_type='application/json')
                    response.status_code = 404

            else:
                response = HttpResponse(json.dumps({"error": {"login": "Bad login"}}),
                                        content_type='application/json')
                response.status_code = 400
        else:
            response = HttpResponse(json.dumps({"error": {"request": "Bad request"}}), content_type='application/json')
            response.status_code = 400


        return response

    elif request.method == "POST":
        if not request.body:
            parameters = {}
        else:
            parameters = json.loads(request.body)
        errors = []
        sender_id = 0
        reciever_id = 0
        if "friend_request" not in parameters or "sender_id" not in parameters["friend_request"] or "reciever_id" not in \
                parameters["friend_request"]:
            errors.append({"field": "request", "reasons": "missing values"})
        else:
            for key in parameters["friend_request"].keys():
                if str(key) == "reciever_id":
                    try:
                        reciever_id = int(parameters["friend_request"]["reciever_id"])
                    except ValueError:
                        errors.append({"field": "reciever_id", "reasons": "bad id"})

                if str(key) == "sender_id":
                    try:
                        sender_id = int(parameters["friend_request"]["sender_id"])
                    except ValueError:
                        errors.append({"field": "sender_id", "reasons": "bad id"})

        if 'HTTP_AUTHORIZATION' in request.META and sender_id!=reciever_id and not errors:
            name_real, password = get_password_name(request)
            user = models.Users.objects.filter(name=name_real).first()
            if user and (user.iduser == sender_id or user.iduser == reciever_id) and check_password(password, user.password):
                if not errors and not models.Users.objects.filter(
                        iduser=reciever_id).exists() or not models.Users.objects.filter(iduser=sender_id).exists():
                    errors.append({"field": "reciever_id/user_id", "reasons": "not a valid user id"})

                if models.Friend_requests.objects.filter((Q(reciever_id=reciever_id) & Q(sender_id=sender_id)) | (
                        Q(reciever_id=sender_id) & Q(sender_id=reciever_id))).exists():
                    errors.append({"field": "reciever_id/user_id", "reasons": "friend request already exists"})

                if models.Friends.objects.filter(
                        (Q(user_2=sender_id) & Q(user_1=reciever_id)) | (Q(user_2=reciever_id) & Q(user_1=sender_id))).exists():
                    errors.append({"field": "users", "reasons": "users are friends"})

                if not errors:
                    current_time = timezone.now()
                    friend_request = models.Friend_requests(sender_id=sender_id, reciever_id=reciever_id,
                                                            created_at=current_time)
                    friend_request.save()
                    response = HttpResponse(content_type='application/json')
                    response.status_code = 201
                else:
                    result = json.dumps({"errors": errors}, cls=DjangoJSONEncoder)
                    response = HttpResponse(result, content_type='application/json')
                    response.status_code = 400
            else:
                response = HttpResponse(json.dumps({"error": {"login": "Bad login"}}),
                                        content_type='application/json')
                response.status_code = 400

        else:
            response = HttpResponse(json.dumps({"error": {"request": "Bad request"}}), content_type='application/json')
            response.status_code = 400

        return response

    if request.method == "DELETE":
        url = str(request.build_absolute_uri())
        parsed_url = urlparse(url)
        params = parse_qs(parsed_url.query)
        errors = []
        sender_id = 0
        reciever_id = 0
        if "reciever_id" in params.keys() and "sender_id" in params.keys():
            for key in params.keys():
                if key == "reciever_id":
                    try:
                        reciever_id = int(params["reciever_id"][0])
                    except ValueError:
                        errors.append("reciever id not integer")
                if key == "sender_id":
                    try:
                        sender_id = int(params["sender_id"][0])
                    except ValueError:
                        errors.append("sender id not integer")
        else:
            errors.append("bad parameters")
            result = json.dumps({"errors": errors}, cls=DjangoJSONEncoder)
            response = HttpResponse(result, content_type='application/json')
            response.status_code = 400

        if 'HTTP_AUTHORIZATION' in request.META and not errors:
            name_real, password = get_password_name(request)
            user = models.Users.objects.filter(name=name_real).first()
            if user and (user.iduser == sender_id or user.iduser == reciever_id) and check_password(password, user.password):
                if models.Friend_requests.objects.filter(sender_id=sender_id).filter(reciever_id=reciever_id).exists():
                    models.Friend_requests.objects.filter(sender_id=sender_id).filter(reciever_id=reciever_id).delete()
                    response = HttpResponse(content_type='application/json')
                    response.status_code = 204
                else:
                    response = HttpResponse(json.dumps({"error": "This friend request does not exists"}),
                                            content_type='application/json')
                    response.status_code = 404
            else:
                response = HttpResponse(json.dumps({"error": {"login": "Bad login"}}),
                                        content_type='application/json')
                response.status_code = 400
        else:
            response = HttpResponse(json.dumps({"error": {"request": "Bad request"}}), content_type='application/json')
            response.status_code = 400

        return response


@csrf_exempt
def register(request):
    errors = []
    regex = r'\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}\b'
    username = None
    passwd1 = None
    email = None
    password = None
    created_at = None
    if request.method == "POST":
        if request.body:
            subject = json.loads(request.body)
        else:
            errors.append({"Error": "no parameters"})
            result = json.dumps({"errors": errors}, cls=DjangoJSONEncoder)
            return HttpResponse(result, content_type='application/json', status=404)

        for key in subject["register"].keys():
            if str(key) == "name":
                username = subject["register"]["name"]
            if str(key) == "password":
                passwd1 = subject["register"]["password"]
            if str(key) == "email":
                email = subject["register"]["email"]

        if not username:
            errors.append({"Name": "is missing"})

        if not passwd1:
            errors.append({"Password": "is missing"})

        if not email:
            errors.append({"Email": "is missing"})
        if not errors:
            password = make_password(passwd1)
            created_at = timezone.now()

        if models.Users.objects.filter(name=username).exists():
            errors.append({"Username": "already used"})

        if models.Users.objects.filter(email=email).exists():
            errors.append({"Email": "already used"})

        if not (re.fullmatch(regex, email)) and email:
            errors.append({"Email": "wrong format"})
            result = json.dumps({"errors": errors}, cls=DjangoJSONEncoder)
            return HttpResponse(result, content_type='application/json', status=422)

        if (len(username) < 4 or len(username) >= 35) and username:
            errors.append({"Username": "too short or too long"})

        if not errors:
            user = models.Users(name=username, email=email, password=password, created_at=created_at,
                                description=None, picture=None)
            user.save()
            return HttpResponse(content_type='application/json', status=204)
        else:
            result = json.dumps({"errors": errors}, cls=DjangoJSONEncoder)

            return HttpResponse(result, content_type='application/json', status=400)


@csrf_exempt
def login(request):

    if request.method == "GET":
        if 'HTTP_AUTHORIZATION' in request.META:
            name, password = get_password_name(request)
        else:
            response = HttpResponse(json.dumps({"error": {"login": "Bad login"}}),
                                    content_type='application/json')
            response.status_code = 400
            return response

        errors = []
        user = models.Users.objects.filter(name=name).first()

        if user:

            if check_password(password, user.password):
                user_info = {
                    'id': user.iduser,
                    'name': user.name,
                    'email': user.email,
                    'description': user.description,
                    'created_at': user.created_at
                }
                if user.picture is not None:
                    try:
                        file = open(user.picture, "rb")
                        picture_byte = str(base64.b64encode(file.read()))
                        user_info["picture"] = picture_byte
                    except Exception:
                        user_info["picture"] = None
                else:
                    user_info["picture"] = None
                success = json.dumps({"result": user_info}, cls=DjangoJSONEncoder)
                return HttpResponse(success,content_type='application/json', status=200)
            else:
                errors.append({"Error": "wrong password"})
                result = json.dumps({"errors": errors}, cls=DjangoJSONEncoder)
                return HttpResponse(result, content_type='application/json', status=401)
        else:
            errors.append({"Error": "no user with this name"})
            result = json.dumps({"errors": errors}, cls=DjangoJSONEncoder)
            return HttpResponse(result, content_type='application/json', status=400)


@csrf_exempt
def statuses(request):
    if request.method == "POST":
        errors = []
        status = None
        id = None
        if request.body:
            subject = json.loads(request.body)
        else:
            errors.append({"Error": "no parameters"})
            result = json.dumps({"errors": errors}, cls=DjangoJSONEncoder)
            return HttpResponse(result, content_type='application/json', status=404)

        if subject:
            for key in subject["status"].keys():
                if str(key) == "text":
                    status = subject["status"]["text"]

                if str(key) == "id":
                    id = subject["status"]["id"]

        if not status or (len(status) > 500):
            errors.append({"Error": "wrong length of text"})

        if id:
            try:
                int(id)
            except ValueError:
                errors.append({"Error": "wrong id"})
                result = json.dumps({"errors": errors}, cls=DjangoJSONEncoder)
                return HttpResponse(result, content_type='application/json', status=400)

        if not id:
            errors.append({"Error": "no id"})

        if 'HTTP_AUTHORIZATION' in request.META and not errors:
            name_real, password = get_password_name(request)
            user = models.Users.objects.filter(name=name_real).first()
            if user:
                if (user.iduser == id) and check_password(password, user.password):
                    user = models.Users.objects.filter(iduser=int(id)).first()

                    if user:
                        current_time = timezone.now()
                        new_status = models.Statuses(owner_id=int(id), text=status, created_at=current_time)
                        new_status.save()
                        return HttpResponse(content_type='application/json', status=204)

                    else:
                        errors.append({"Error": "no user found"})
                        result = json.dumps({"errors": errors}, cls=DjangoJSONEncoder)
                        return HttpResponse(result, content_type='application/json', status=404)
                else:
                    response = HttpResponse(json.dumps({"error": {"login": "Bad login"}}),
                                            content_type='application/json')
                    response.status_code = 400
                    return response
            else:
                response = HttpResponse(json.dumps({"error": {"Name": "no user found"}}),
                                        content_type='application/json')
                response.status_code = 400
                return response

        else:
            response = HttpResponse(json.dumps({"error": {"request": "Bad request"}}),content_type='application/json')
            response.status_code = 400
            return response

    if request.method == "GET":
        id = request.GET.get('owner')
        statuses = []
        errors = []

        if id:
            try:
              int(id)
            except ValueError:
              errors.append({"Error": "wrong id"})
              result = json.dumps({"errors": errors}, cls=DjangoJSONEncoder)
              return HttpResponse(result, content_type='application/json', status=400)

        if 'HTTP_AUTHORIZATION' in request.META and not errors:
            name_real, password = get_password_name(request)
            user = models.Users.objects.filter(name=name_real).first()
            if user and user.iduser == int(id) and check_password(password, user.password):
                if models.Users.objects.filter(iduser=int(id)).exists():
                    friends = list(models.Friends.objects.filter(user_1=id).values_list('user_2', flat=True))
                    friends += list(models.Friends.objects.filter(user_2=id).values_list('user_1', flat=True))
                    friends.append(id)
                    result = models.Statuses.objects.filter(owner_id__in=friends).order_by('-created_at').values_list('idstatuses', 'text', 'created_at', 'owner_id')
                    for status in result:
                        user = models.Users.objects.filter(iduser=status[3]).first()
                        nested_dict = {
                            'status_id': status[0],
                            'owner_name': user.name,
                            'text': status[1],
                            'created_at': status[2]
                        }
                        statuses.append(nested_dict)
                    if statuses:
                        result = json.dumps({"results": statuses}, cls=DjangoJSONEncoder)
                        return HttpResponse(result, content_type='application/json', status=200)
                else:
                    errors.append({"Error": "no user found"})
                    result = json.dumps({"errors": errors}, cls=DjangoJSONEncoder)
                    return HttpResponse(result, content_type='application/json', status=404)
            else:
                response = HttpResponse(json.dumps({"error": {"login": "Bad login"}}),
                                        content_type='application/json')
                response.status_code = 400
                return response
        else:
            response = HttpResponse(json.dumps({"error": {"request": "Bad request"}}),
                                    content_type='application/json')
            response.status_code = 400
            return response

    if request.method == "DELETE":
        status_id = request.GET.get('status_id')

        if status_id:
            try:
                int(status_id)
            except ValueError:
                result = json.dumps({"errors": {"Error": "wrong id"}}, cls=DjangoJSONEncoder)
                return HttpResponse(result, content_type='application/json', status=400)

        if 'HTTP_AUTHORIZATION' in request.META:
            name_real, password = get_password_name(request)
            user = models.Users.objects.filter(name=name_real).first()
            status = models.Statuses.objects.filter(idstatuses=status_id).first()
            if user and status:
                id = models.Users.objects.filter(iduser=status.owner_id).first().iduser
                if (user.iduser == int(id)) and (status.owner_id == int(id)) and check_password(password, user.password):
                    if status_id and models.Statuses.objects.filter(idstatuses=status_id).exists():
                        models.Statuses.objects.filter(idstatuses=status_id).delete()
                        return HttpResponse(content_type='application/json', status=204)
                    else:
                        result = json.dumps({"errors": {"Error": "no status found"}}, cls=DjangoJSONEncoder)
                        return HttpResponse(result, content_type='application/json', status=404)
                else:
                    response = HttpResponse(json.dumps({"error": {"login": "Bad login"}}), content_type='application/json')
                    response.status_code = 400
                    return response

            else:
                response = HttpResponse(json.dumps({"error": {"request": "Bad request"}}), content_type='application/json')
                response.status_code = 400
                return response
        else:
            result = json.dumps({"errors": {"Error": "Bad request"}}, cls=DjangoJSONEncoder)
            return HttpResponse(result, content_type='application/json', status=404)


@csrf_exempt
def advertisements(request):
    if request.method == "POST":
        errors = []
        game_id = None
        owner_id = None
        rank_id = None
        status = None

        if request.body:
            subject = json.loads(request.body)
        else:
            result = json.dumps({"errors": {"Error": "no parameters"}}, cls=DjangoJSONEncoder)
            return HttpResponse(result, content_type='application/json', status=404)

        if subject:
            for key in subject["advertisement"].keys():
                if str(key) == "owner_id":
                    owner_id = subject["advertisement"]["owner_id"]

                if str(key) == "game_id":
                    game_id = subject["advertisement"]["game_id"]

                if str(key) == "rank_id":
                    rank_id = subject["advertisement"]["rank_id"]

                if str(key) == "text":
                    status = subject["advertisement"]["text"]

        if game_id:
            try:
                int(game_id)
            except ValueError:
                result = json.dumps({"errors": {"Error": "wrong id"}}, cls=DjangoJSONEncoder)
                return HttpResponse(result, content_type='application/json', status=400)

        if rank_id:
            try:
                int(rank_id)
            except ValueError:
                result = json.dumps({"errors": {"Error": "wrong id"}}, cls=DjangoJSONEncoder)
                return HttpResponse(result, content_type='application/json', status=400)

        if owner_id:
            try:
                int(owner_id)
            except ValueError:
                result = json.dumps({"errors": {"Error": "wrong id"}}, cls=DjangoJSONEncoder)
                return HttpResponse(result, content_type='application/json', status=400)

        if not status or (len(status) > 500):
            result = json.dumps({"errors": {"Error": "bad lenght "}}, cls=DjangoJSONEncoder)
            return HttpResponse(result, content_type='application/json', status=400)

        if 'HTTP_AUTHORIZATION' in request.META:
            name_real, password = get_password_name(request)
            user = models.Users.objects.filter(name=name_real).first()
            if user:
                if (user.iduser == owner_id) and check_password(password, user.password):

                    if not errors and not models.Users.objects.filter(iduser=owner_id).exists():
                        errors.append({"Error": "no user with this id"})

                    if not errors and not models.Games.objects.filter(idgames=game_id).exists():
                        errors.append({"Error": "no game with this id"})

                    if not errors and not models.Ranks.objects.filter(idranks=rank_id).exists():
                        errors.append({"Error": "no rank with this id"})

                    else:
                        game = models.Games.objects.filter(idgames=game_id).first()

                    if not errors and not models.Ranks.objects.filter(game_id=game.idgames, idranks=rank_id).exists():
                        errors.append({"Error": "this rank is not valid for this game"})

                    if not errors:
                        current_time = timezone.now()
                        new_advertisement = models.Advertisements(owner_id=owner_id, rank_id=rank_id, game_id=game_id, text=status, created_at=current_time)
                        new_advertisement.save()
                        return HttpResponse(content_type='application/json', status=204)

                else:
                    response = HttpResponse(json.dumps({"error": {"login": "Bad login"}}),
                                            content_type='application/json')
                    response.status_code = 400
                    return response

            else:
                response = HttpResponse(json.dumps({"error": {"Name": "no user found"}}),
                                        content_type='application/json')
                response.status_code = 400
                return response

        else:
            response = HttpResponse(json.dumps({"error": {"request": "Bad request"}}), content_type='application/json')
            response.status_code = 400
            return response



    if request.method == "GET":
        owner = request.GET.get('user_id')
        game = request.GET.get('game')
        rank = request.GET.get('rank')
        errors = []

        if game:
            try:
                int(game)
            except ValueError:
                result = json.dumps({"errors": {"Error": "wrong id"}}, cls=DjangoJSONEncoder)
                return HttpResponse(result, content_type='application/json', status=400)

        if rank:
            try:
                int(rank)
            except ValueError:
                result = json.dumps({"errors": {"Error": "wrong id"}}, cls=DjangoJSONEncoder)
                return HttpResponse(result, content_type='application/json', status=400)

        if owner:
            try:
                int(owner)
            except ValueError:
                result = json.dumps({"errors": {"Error": "wrong id"}}, cls=DjangoJSONEncoder)
                return HttpResponse(result, content_type='application/json', status=400)

        if 'HTTP_AUTHORIZATION' in request.META and owner and not errors:
            name_real, password = get_password_name(request)
            user = models.Users.objects.filter(name=name_real).first()
            if user and (user.iduser == int(owner)) and check_password(password, user.password):
                if owner and not game and not rank:

                    if models.Users.objects.filter(iduser=owner).exists():

                        my_ad = []
                        all_ad = []
                        result = models.Advertisements.objects.filter(owner_id=owner).distinct("created_at").order_by("-created_at").values_list('idadvertisement', 'rank_id', 'game_id', 'owner_id', 'text', 'created_at')
                        result2 = models.Games.objects.values_list('idgames', 'name')
                        result3 = models.Ranks.objects.values_list('idranks', 'name')
                        result4 = models.Users.objects.filter(iduser=owner).values_list('iduser','name')
                        if result:

                            for ad in result:
                                for ad2 in result2:
                                    if ad2[0] == ad[2]:
                                        game_name = ad2[1]

                                for ad3 in result3:
                                    if ad3[0] == ad[1]:
                                        rank_name = ad3[1]

                                for ad4 in result4:
                                    if ad4[0] == ad[3]:
                                        owner_name = ad4[1]
                                nested_dict = {
                                    'id': ad[0],
                                    'name': owner_name,
                                    'rank_id': ad[1],
                                    'rank_name': rank_name,
                                    'game_id': ad[2],
                                    'game_name': game_name,
                                    'owner_id': ad[3],
                                    'text': ad[4],
                                    'created_at': ad[5]
                                }
                                my_ad.append(nested_dict)

                        all_result = models.Advertisements.objects.exclude(owner_id=owner).distinct("created_at").order_by("-created_at").values_list('idadvertisement', 'rank_id', 'game_id', 'owner_id', 'text', 'created_at')
                        result2 = models.Games.objects.values_list('idgames', 'name')
                        result3 = models.Ranks.objects.values_list('idranks', 'name')
                        result4 = models.Users.objects.exclude(iduser=owner).values_list('iduser','name')
                        if all_result:
                            for ad in all_result:
                                for ad2 in result2:
                                    if ad2[0] == ad[2]:
                                        game_name = ad2[1]

                                for ad3 in result3:
                                    if ad3[0] == ad[1]:
                                        rank_name = ad3[1]

                                for ad4 in result4:
                                    if ad4[0] == ad[3]:
                                        user_name = ad4[1]

                                nested_dict = {
                                    'id': ad[0],
                                    'name': user_name,
                                    'rank_id': ad[1],
                                    'rank_name': rank_name,
                                    'game_id': ad[2],
                                    'game_name': game_name,
                                    'owner_id': ad[3],
                                    'text': ad[4],
                                    'created_at': ad[5]
                                }
                                all_ad.append(nested_dict)

                        if not all_result and result:
                            final_ad = json.dumps({"owned": my_ad}, cls=DjangoJSONEncoder)
                            return HttpResponse(final_ad, content_type='application/json', status=200)

                        elif all_result and not result:
                            final_ad = json.dumps({"results": all_ad}, cls=DjangoJSONEncoder)
                            return HttpResponse(final_ad, content_type='application/json', status=200)

                        if all_result and result:
                             final_ad = json.dumps({"ownned": my_ad, "results": all_ad}, cls=DjangoJSONEncoder)
                             return HttpResponse(final_ad, content_type='application/json', status=200)
                        else:
                            nresult = json.dumps({"errors": {"Error": "no advertisement found"}}, cls=DjangoJSONEncoder)
                            return HttpResponse(nresult, content_type='application/json', status=404)

                    else:
                        nresult = json.dumps({"errors": {"Error": "no user found"}}, cls=DjangoJSONEncoder)
                        return HttpResponse(nresult, content_type='application/json', status=404)

                else:
                    if not rank:
                        errors.append({"Error": "no rank found"})

                    if not game:
                        errors.append({"Error": "no game found"})

                    if errors:
                        result = json.dumps({"errors": errors}, cls=DjangoJSONEncoder)
                        return HttpResponse(result, content_type='application/json', status=404)

                    if models.Users.objects.filter(iduser=owner).exists():
                        all_ad = []
                        all_result = models.Advertisements.objects.filter(game_id=game, rank_id= rank).exclude(owner_id=owner).distinct("created_at").order_by("-created_at").values_list('idadvertisement', 'rank_id', 'game_id', 'owner_id','text', 'created_at')

                        result2 = models.Games.objects.values_list('idgames', 'name')
                        result3 = models.Ranks.objects.values_list('idranks', 'name')
                        result4 = models.Users.objects.exclude(iduser=owner).values_list('iduser', 'name')

                        if all_result:
                            for ad in all_result:
                                for ad2 in result2:
                                    if ad2[0] == ad[2]:
                                        game_name = ad2[1]

                                for ad3 in result3:
                                    if ad3[0] == ad[1]:
                                        rank_name = ad3[1]

                                for ad4 in result4:
                                    if ad4[0] == ad[3]:
                                        user_name = ad4[1]

                                nested_dict = {
                                    'id': ad[0],
                                    'name': user_name,
                                    'rank_id': ad[1],
                                    'rank_name': rank_name,
                                    'game_id': ad[2],
                                    'game_name': game_name,
                                    'owner_id': ad[3],
                                    'text': ad[4]
                                }
                                all_ad.append(nested_dict)

                        if all_result:
                            final_ad = json.dumps({"results": all_ad}, cls=DjangoJSONEncoder)
                            return HttpResponse(final_ad, content_type='application/json', status=200)
                        else:
                            errors.append({"Error": "no advertisement found"})
                            nresult = json.dumps({"errors": errors}, cls=DjangoJSONEncoder)
                            return HttpResponse(nresult, content_type='application/json', status=404)

                    else:
                        errors.append({"Error": "no user found"})
                        nresult = json.dumps({"errors": errors}, cls=DjangoJSONEncoder)
                        return HttpResponse(nresult, content_type='application/json', status=404)

            else:
                response = HttpResponse(json.dumps({"error": {"login": "Bad login"}}),
                                        content_type='application/json')
                response.status_code = 400
                return response
        else:
            response = HttpResponse(json.dumps({"error": {"request": "Bad request"}}), content_type='application/json')
            response.status_code = 400
            return response

    if request.method == "DELETE":
        ad_id = request.GET.get('ad_id')
        errors = []

        if ad_id:
            try:
                int(ad_id)
            except ValueError:
                errors.append({"Error": "wrong id"})
                result = json.dumps({"errors": errors}, cls=DjangoJSONEncoder)
                return HttpResponse(result, content_type='application/json', status=400)

        if 'HTTP_AUTHORIZATION' in request.META and not errors:
            name_real, password = get_password_name(request)
            user = models.Users.objects.filter(name=name_real).first()
            if user:
                if models.Advertisements.objects.filter(idadvertisement=ad_id).exists():
                    owner_id = models.Advertisements.objects.filter(idadvertisement=ad_id).values('owner_id')[0]['owner_id']
                    print(owner_id)
                    if (user.iduser == owner_id) and check_password(password, user.password):
                        if models.Advertisements.objects.filter(owner_id=owner_id).exists():
                            models.Advertisements.objects.filter(idadvertisement=ad_id).delete()
                            return HttpResponse(content_type='application/json', status=204)

                        else:
                            errors.append({"Error": "wrong owner for this ad"})
                            result = json.dumps({"errors": errors}, cls=DjangoJSONEncoder)
                            return HttpResponse(result, content_type='application/json', status=404)
                    else:
                        response = HttpResponse(json.dumps({"error": {"login": "Bad login"}}),
                                                content_type='application/json')
                        response.status_code = 400
                        return response

                else:
                    errors.append({"Error": "no id found"})
                    result = json.dumps({"errors": errors}, cls=DjangoJSONEncoder)
                    return HttpResponse(result, content_type='application/json', status=404)

            else:
                response = HttpResponse(json.dumps({"error": {"Name": "no user found"}}),
                                        content_type='application/json')
                response.status_code = 400
                return response

        else:
            response = HttpResponse(json.dumps({"error": {"request": "Bad request"}}), content_type='application/json')
            response.status_code = 400
            return response


@csrf_exempt
def ranks(request):
    if request.method == "GET":
        errors = []
        result = []
        game = request.GET.get('game_id')


        if models.Games.objects.filter(idgames=game).exists():
            if models.Ranks.objects.filter(game_id=game).exists():
                rank = models.Ranks.objects.filter(game_id=game).distinct("idranks").order_by("idranks").values_list('idranks','name', 'tier')
                if rank:
                    for next_rank in rank:
                        nested_dict = {
                            'id' : next_rank[0],
                            'rank': next_rank[1],
                            'tier': next_rank[2]
                        }
                        result.append(nested_dict)

                    if result:
                        final_result = json.dumps({"results": result}, cls=DjangoJSONEncoder)
                        return HttpResponse(final_result, content_type='application/json', status=200)
                    else:
                        errors.append({"Error": "no rank found"})
                        nresult = json.dumps({"errors": errors}, cls=DjangoJSONEncoder)
                        return HttpResponse(nresult, content_type='application/json', status=404)
                else:
                    errors.append({"Error": "no rank found"})
                    nresult = json.dumps({"errors": errors}, cls=DjangoJSONEncoder)
                    return HttpResponse(nresult, content_type='application/json', status=404)
            else:
                errors.append({"Error": "no rank found"})
                nresult = json.dumps({"errors": errors}, cls=DjangoJSONEncoder)
                return HttpResponse(nresult, content_type='application/json', status=404)
        else:
            errors.append({"Error": "no game found"})
            nresult = json.dumps({"errors": errors}, cls=DjangoJSONEncoder)
            return HttpResponse(nresult, content_type='application/json', status=404)

@csrf_exempt
def games(request):
    if request.method == "GET":
        records = []
        result = models.Games.objects.all().order_by('name')
        for record in result:
            records.append({"id": record.idgames, "name": record.name, "description": record.description})
        result = json.dumps({"results": records}, cls=DjangoJSONEncoder)
        response = HttpResponse(result, content_type='application/json')
        response.status_code = 200
        return response





