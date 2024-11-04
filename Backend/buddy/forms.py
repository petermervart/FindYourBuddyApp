from django.forms import ModelForm
from django.contrib.auth.hashers import make_password
from . import models
from django import forms
import uuid
import datetime


class RegisterForm(ModelForm):
    username = forms.CharField(max_length=35)
    password = forms.CharField(widget=forms.Textarea)
    emails = forms.CharField(max_length=35)

    class Meta:
        model = models.Users
        fields = ("username", "emails", "password")
    """
    def val(self):
        values = {
            "user": self.cleaned_data.get("username"),
            "iduser": self.cleaned_data.get("iduser"),
            "password": self.cleaned_data.get("passwd"),
            "created_at": self.cleaned_data.get("created_at"),
            "email": self.get("emails"),
        }
        return values
    """